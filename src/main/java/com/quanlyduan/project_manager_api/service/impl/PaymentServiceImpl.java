package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CheckoutRequest;
import com.quanlyduan.project_manager_api.dto.response.CheckoutResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.TransactionDetailResponse;
import com.quanlyduan.project_manager_api.dto.response.TransactionListResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.BillingCycle;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TransactionStatus;
import com.quanlyduan.project_manager_api.repository.*;
import com.quanlyduan.project_manager_api.service.PaymentService;
import com.quanlyduan.project_manager_api.service.EmailService; // THÊM IMPORT EMAIL

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;       // IMPORT CHUẨN
import vn.payos.model.webhooks.WebhookData;   // IMPORT CHUẨN

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PayOS payOS;
    private final EmailService emailService; // INJECT EMAIL SERVICE

    @Value("${payos.return-url}")
    private String defaultReturnUrl;

    @Value("${payos.cancel-url}")
    private String defaultCancelUrl;

    // =================================================================================
    // 1. TẠO LINK THANH TOÁN (KÈM PRORATION)
    // =================================================================================
    @Override
    @Transactional
    public CheckoutResponse createPaymentLink(Integer companyId, Integer userId, CheckoutRequest request) {
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Công ty."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User."));
        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Gói cước."));

        BillingCycle cycle = BillingCycle.valueOf(request.getBillingCycle().toUpperCase());

        // 1. Tính giá gốc của gói muốn mua
        BigDecimal originalAmount = (cycle == BillingCycle.YEARLY) ? plan.getYearlyPrice() : plan.getMonthlyPrice();
        if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            if (cycle == BillingCycle.YEARLY && plan.getMonthlyPrice() != null) {
                originalAmount = plan.getMonthlyPrice().multiply(new BigDecimal(12));
            } else {
                throw new BadRequestException("Gói cước này không yêu cầu thanh toán.");
            }
        }

        CompanySubscription currentSub = company.getSubscriptions().stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        // 2. LOGIC PRORATION: KHẤU TRỪ VÀ QUY ĐỔI NGÀY
        LocalDateTime now = LocalDateTime.now();
        BigDecimal remainingValue = BigDecimal.ZERO;
        
        if (currentSub != null && !currentSub.getPlan().getId().equals(plan.getId()) 
            && currentSub.getCurrentPeriodEnd() != null 
            && currentSub.getCurrentPeriodEnd().isAfter(now)) {
            
            long remainingDays = ChronoUnit.DAYS.between(now, currentSub.getCurrentPeriodEnd());
            if (remainingDays > 0) {
                BigDecimal oldYearlyPrice = currentSub.getPlan().getYearlyPrice();
                if (oldYearlyPrice == null || oldYearlyPrice.compareTo(BigDecimal.ZERO) == 0) {
                    oldYearlyPrice = currentSub.getPlan().getMonthlyPrice().multiply(new BigDecimal(12));
                }
                
                BigDecimal dailyRate = oldYearlyPrice.divide(new BigDecimal(365), 2, RoundingMode.HALF_UP);
                remainingValue = dailyRate.multiply(new BigDecimal(remainingDays));
                
                log.info("🔄 Chuyển gói: Công ty [{}] còn dư {} ngày gói [{}]. Giá trị: {} VNĐ", 
                        company.getName(), remainingDays, currentSub.getPlan().getName(), remainingValue);
            }
        }

        BigDecimal finalAmountToPay = originalAmount.subtract(remainingValue);
        long orderCode = System.currentTimeMillis(); 
        String transactionCode = String.valueOf(orderCode);
        String returnUrl = request.getReturnUrl() != null ? request.getReturnUrl() : defaultReturnUrl;

        // KỊCH BẢN ĐẶC BIỆT: KHÁCH CÒN QUÁ NHIỀU TIỀN DƯ (Thu < 2000đ)
        if (finalAmountToPay.compareTo(new BigDecimal(2000)) < 0) {
            Transaction transaction = Transaction.builder()
                    .company(company)
                    .plan(plan)
                    .subscription(currentSub) 
                    .transactionCode(transactionCode)
                    .amount(BigDecimal.ZERO) 
                    .currency("VND")
                    .billingCycle(cycle)
                    .paymentMethod("SYSTEM_CREDIT") 
                    .status(TransactionStatus.SUCCESS)
                    .createdBy(user)
                    .build();
            transactionRepository.save(transaction);
            
            if (currentSub != null) currentSub.setStatus(SubscriptionStatus.EXPIRED);
            
            LocalDateTime newEndDate = (cycle == BillingCycle.YEARLY) ? now.plusYears(1) : now.plusMonths(1);
            
            if (remainingValue.compareTo(originalAmount) > 0) {
                BigDecimal extraValue = remainingValue.subtract(originalAmount);
                BigDecimal newYearlyPrice = plan.getYearlyPrice() != null ? plan.getYearlyPrice() : plan.getMonthlyPrice().multiply(new BigDecimal(12));
                BigDecimal newDailyRate = newYearlyPrice.divide(new BigDecimal(365), 2, RoundingMode.HALF_UP);
                
                if (newDailyRate.compareTo(BigDecimal.ZERO) > 0) {
                    long bonusDays = extraValue.divide(newDailyRate, 0, RoundingMode.DOWN).longValue();
                    newEndDate = newEndDate.plusDays(bonusDays);
                }
            }
            
            CompanySubscription newSub = new CompanySubscription();
            newSub.setCompany(company);
            newSub.setPlan(plan);
            newSub.setStatus(SubscriptionStatus.ACTIVE);
            newSub.setCurrentPeriodStart(now);
            newSub.setCurrentPeriodEnd(newEndDate);
            company.getSubscriptions().add(newSub);
            companyRepository.save(company);

            // GỬI BIÊN LAI NỘI BỘ
            sendReceiptEmail(transaction, company, newSub);

            return CheckoutResponse.builder()
                    .transactionCode(transactionCode)
                    .checkoutUrl(returnUrl) 
                    .build();
        }

        // KỊCH BẢN BÌNH THƯỜNG: CẦN GỌI PAYOS THANH TOÁN
        Transaction transaction = Transaction.builder()
                .company(company)
                .plan(plan)
                .subscription(currentSub) 
                .transactionCode(transactionCode)
                .amount(finalAmountToPay) 
                .currency("VND")
                .billingCycle(cycle)
                .paymentMethod("PAYOS")
                .status(TransactionStatus.PENDING)
                .createdBy(user)
                .build();
        transactionRepository.save(transaction);

        try {
            String cancelUrl = request.getCancelUrl() != null ? request.getCancelUrl() : defaultCancelUrl;
            String description = "Nang cap goi " + plan.getName();
            if (description.length() > 25) description = description.substring(0, 25);

            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(finalAmountToPay.longValue())
                    .description(description) 
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .build();

            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

            return CheckoutResponse.builder()
                    .transactionCode(transactionCode)
                    .checkoutUrl(data.getCheckoutUrl())
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi gọi API PayOS: ", e); 
            throw new BadRequestException("Không thể tạo link thanh toán lúc này.");
        }
    }

    // =================================================================================
    // 2. XỬ LÝ WEBHOOK TỪ PAYOS (VÀ GỬI EMAIL BIÊN LAI)
    // =================================================================================
    @Override
    @Transactional
    public void processWebhook(Webhook webhookBody) {
        try {
            if (webhookBody.getSignature() == null || webhookBody.getSignature().isEmpty()) {
                log.warn("Bỏ qua Webhook do không có chữ ký (signature).");
                return; 
            }

            WebhookData data = payOS.webhooks().verify(webhookBody);
            log.info("Nhận được Webhook từ PayOS. Mã đơn hàng: {}", data.getOrderCode());

            if ("00".equals(data.getCode())) {
                String transactionCode = String.valueOf(data.getOrderCode());

                Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch: " + transactionCode));

                if (transaction.getStatus() == TransactionStatus.PENDING) {
                    
                    transaction.setStatus(TransactionStatus.SUCCESS);
                    transactionRepository.save(transaction);
                    
                    // HỦY CÁC MÃ QR PENDING CŨ CỦA CÔNG TY
                    List<Transaction> oldPendingTransactions = transactionRepository
                            .findByCompanyIdAndStatusAndIdNot(
                                    transaction.getCompany().getId(), 
                                    TransactionStatus.PENDING, 
                                    transaction.getId()
                            );
                            
                    if (!oldPendingTransactions.isEmpty()) {
                        oldPendingTransactions.forEach(tx -> tx.setStatus(TransactionStatus.CANCELLED));
                        transactionRepository.saveAll(oldPendingTransactions);
                    }

                    // LOGIC NÂNG CẤP GÓI
                    Company company = transaction.getCompany();
                    SubscriptionPlan purchasedPlan = transaction.getPlan();
                    BillingCycle cycle = transaction.getBillingCycle();

                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime baseDate = now;

                    CompanySubscription currentSub = company.getSubscriptions().stream()
                            .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                            .findFirst()
                            .orElse(null);

                    if (currentSub != null) {
                        boolean isSamePlan = currentSub.getPlan().getId().equals(purchasedPlan.getId());
                        boolean isNotExpired = currentSub.getCurrentPeriodEnd() != null && 
                                               currentSub.getCurrentPeriodEnd().isAfter(now);

                        if (isSamePlan && isNotExpired) {
                            baseDate = currentSub.getCurrentPeriodEnd();
                        }
                        currentSub.setStatus(SubscriptionStatus.EXPIRED);
                    }

                    LocalDateTime newEndDate;
                    if (cycle == BillingCycle.YEARLY) {
                        newEndDate = baseDate.plusYears(1);
                    } else {
                        newEndDate = baseDate.plusMonths(1);
                    }

                    CompanySubscription newSubscription = new CompanySubscription();
                    newSubscription.setCompany(company);
                    newSubscription.setPlan(purchasedPlan);
                    newSubscription.setStatus(SubscriptionStatus.ACTIVE);
                    newSubscription.setCurrentPeriodStart(now);
                    newSubscription.setCurrentPeriodEnd(newEndDate);
                    
                    company.getSubscriptions().add(newSubscription);
                    companyRepository.save(company);

                    log.info("🎉 HOÀN TẤT: Công ty [{}] đã chuyển sang gói [{}] mới.", company.getName(), purchasedPlan.getName());

                    // 🚀 GỬI EMAIL BIÊN LAI (BẤT ĐỒNG BỘ) CHẠY Ở ĐÂY
                    sendReceiptEmail(transaction, company, newSubscription);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý Webhook hoặc Chữ ký không hợp lệ: ", e);
            throw new BadRequestException("Webhook verification failed.");
        }
    }

    // =================================================================================
    // 3. API KHÁCH CHỦ ĐỘNG HỦY ĐƠN HÀNG
    // =================================================================================
    @Override
    @Transactional
    public void cancelPendingTransaction(String transactionCode, Integer companyId, String reason) {
        Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giao dịch: " + transactionCode));

        if (!transaction.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Access Denied: Bạn không có quyền hủy giao dịch này.");
        }

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new BadRequestException("Giao dịch này không ở trạng thái chờ thanh toán nên không thể hủy.");
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);

        try {
            payOS.paymentRequests().cancel(Long.parseLong(transactionCode), reason);
        } catch (Exception e) {
            log.warn("Đã hủy đơn [{}] ở Local, nhưng PayOS báo lỗi: {}", transactionCode, e.getMessage());
        }
    }

    // =================================================================================
    // 4. CRON JOB: TỰ ĐỘNG DỌN RÁC ĐƠN PENDING (QUÁ 15 PHÚT)
    // =================================================================================
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cleanupExpiredTransactions() {
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        List<Transaction> expiredTransactions = transactionRepository
                .findByStatusAndCreatedAtBefore(TransactionStatus.PENDING, fifteenMinutesAgo);

        if (!expiredTransactions.isEmpty()) {
            expiredTransactions.forEach(tx -> tx.setStatus(TransactionStatus.CANCELLED));
            transactionRepository.saveAll(expiredTransactions);
            log.info("🧹 Cron Job: Đã quét và tự động HỦY {} đơn hàng PENDING do quá hạn.", expiredTransactions.size());
        }
    }

    // =========================================================================
    // 5. API LẤY DANH SÁCH GIAO DỊCH
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<TransactionListResponse> getTransactionHistory(
            Integer companyId, int page, int size, String status, LocalDateTime startDate, LocalDateTime endDate) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        TransactionStatus txStatus = (status != null && !status.equalsIgnoreCase("ALL")) 
                                     ? TransactionStatus.valueOf(status.toUpperCase()) : null;

        Page<Transaction> transactions = transactionRepository.filterTransactions(
                companyId, txStatus, startDate, endDate, pageable);

        Page<TransactionListResponse> dtoPage = transactions.map(tx -> TransactionListResponse.builder()
                .transactionCode(tx.getTransactionCode())
                .planName(tx.getPlan().getName())
                .amount(tx.getAmount())
                .status(tx.getStatus().toString())
                .createdAt(tx.getCreatedAt())
                .build());

        return new PageResponseDTO<>(dtoPage);
    }

    // =========================================================================
    // 6. API XEM CHI TIẾT GIAO DỊCH
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransactionDetail(String transactionCode, Integer companyId) {
        Transaction tx = transactionRepository.findByTransactionCodeAndCompanyId(transactionCode, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch hoặc bạn không có quyền xem."));

        BigDecimal originalPrice = tx.getBillingCycle() == BillingCycle.YEARLY 
                                   ? tx.getPlan().getYearlyPrice() 
                                   : tx.getPlan().getMonthlyPrice();
                                   
        BigDecimal deducted = originalPrice.subtract(tx.getAmount());
        if (deducted.compareTo(BigDecimal.ZERO) < 0) deducted = BigDecimal.ZERO;

        Company company = tx.getCompany();
        CompanySubscription activeSub = company.getSubscriptions().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE || s.getStatus() == SubscriptionStatus.PAST_DUE)
                .findFirst()
                .orElse(null);

        TransactionDetailResponse.TransactionDetailResponseBuilder builder = TransactionDetailResponse.builder()
                .transactionCode(tx.getTransactionCode())
                .createdAt(tx.getCreatedAt())
                .paidAt(tx.getPaidAt())
                .status(tx.getStatus().toString())
                .paymentMethod(tx.getPaymentMethod())
                .originalPrice(originalPrice)
                .deductedAmount(deducted)
                .finalPaidAmount(tx.getAmount())
                .gatewayReferenceCode(tx.getGatewayTransactionId());

        if (activeSub != null) {
            builder.currentPlanName(activeSub.getPlan().getName())
                   .subscriptionStatus(activeSub.getStatus().toString())
                   .currentPeriodStart(activeSub.getCurrentPeriodStart())
                   .currentPeriodEnd(activeSub.getCurrentPeriodEnd())
                   .isPendingCancel(Boolean.TRUE.equals(activeSub.getCancelAtPeriodEnd()));
        }

        return builder.build();
    }

    // ============================================================================
    // ✉️ HÀM HELPER: TẠO VÀ GỬI EMAIL BIÊN LAI
    // ============================================================================
    private void sendReceiptEmail(Transaction tx, Company company, CompanySubscription sub) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String paymentDate = tx.getCreatedAt().format(dateFormatter);
        String nextBillingDate = sub.getCurrentPeriodEnd() != null 
                ? sub.getCurrentPeriodEnd().format(dateFormatter) 
                : "Không giới hạn";
                
        NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormatter.format(tx.getAmount()) + " VNĐ";

        String subject = "Biên lai thanh toán: Nâng cấp gói " + tx.getPlan().getName() + " thành công";

        // 🚀 Lấy thông tin người đã trực tiếp tạo ra giao dịch này (Giám đốc / Kế toán)
        User payer = tx.getCreatedBy();

        String emailBody = String.format(
            "<div style=\"font-family: Arial, sans-serif; max-width: 650px; margin: 0 auto; border: 1px solid #e1e5eb; border-radius: 8px; overflow: hidden;\">" +
            "  <div style=\"background-color: #2c3e50; padding: 25px; text-align: center; color: white;\">" +
            "    <h1 style=\"margin: 0; font-size: 24px;\">BIÊN LAI THANH TOÁN</h1>" +
            "    <p style=\"margin: 10px 0 0 0; font-size: 14px; opacity: 0.9;\">Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!</p>" +
            "  </div>" +
            "  <div style=\"padding: 30px;\">" +
            // Thay đổi lời chào thành tên của người thanh toán thay vì tên công ty
            "    <p>Xin chào <b>%s</b>,</p>" + 
            "    <p>Chúng tôi xác nhận đã nhận được thanh toán của bạn cho hệ thống quản trị dự án tại công ty <b>%s</b>. Gói cước của bạn đã được kích hoạt thành công.</p>" +
            "    <table style=\"width: 100%%; border-collapse: collapse; margin: 25px 0; background-color: #f8f9fa; border-radius: 5px;\">" +
            "      <tr>" +
            "        <td style=\"padding: 15px; border-bottom: 1px solid #e1e5eb;\"><b>Mã giao dịch:</b></td>" +
            "        <td style=\"padding: 15px; text-align: right; border-bottom: 1px solid #e1e5eb; font-family: monospace;\">#%s</td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td style=\"padding: 15px; border-bottom: 1px solid #e1e5eb;\"><b>Ngày thanh toán:</b></td>" +
            "        <td style=\"padding: 15px; text-align: right; border-bottom: 1px solid #e1e5eb;\">%s</td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td style=\"padding: 15px; border-bottom: 1px solid #e1e5eb;\"><b>Gói dịch vụ:</b></td>" +
            "        <td style=\"padding: 15px; text-align: right; border-bottom: 1px solid #e1e5eb; color: #3498db; font-weight: bold;\">%s</td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td style=\"padding: 15px; border-bottom: 1px solid #e1e5eb;\"><b>Hạn sử dụng tiếp theo:</b></td>" +
            "        <td style=\"padding: 15px; text-align: right; border-bottom: 1px solid #e1e5eb;\">%s</td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td style=\"padding: 15px; font-size: 18px;\"><b>TỔNG THANH TOÁN:</b></td>" +
            "        <td style=\"padding: 15px; text-align: right; font-size: 18px; color: #27ae60; font-weight: bold;\">%s</td>" +
            "      </tr>" +
            "    </table>" +
            "    <p style=\"color: #7f8c8d; font-size: 13px; line-height: 1.5;\">" +
            "      * Đây là biên lai điện tử được xuất tự động từ hệ thống. Bạn có thể xem lại toàn bộ lịch sử giao dịch tại mục Cài đặt Công ty." +
            "    </p>" +
            "  </div>" +
            "  <div style=\"background-color: #f4f6f8; padding: 15px; text-align: center; color: #7f8c8d; font-size: 12px;\">" +
            "    <p style=\"margin: 0;\">© 2026 Worknet - Hệ thống Quản trị Dự án</p>" +
            "  </div>" +
            "</div>",
            payer.getFullName(), // 1. Truyền tên người dùng
            company.getName(),   // 2. Truyền tên công ty
            tx.getTransactionCode(),
            paymentDate,
            tx.getPlan().getName(),
            nextBillingDate,
            formattedAmount
        );

        // 🚀 Gửi email trực tiếp cho người đã thao tác
        emailService.sendEmail(payer.getEmail(), subject, emailBody);
        log.info("📧 Đã gửi Email Biên lai cho đơn hàng {} đến người thanh toán: {}", tx.getTransactionCode(), payer.getEmail());
    }
}