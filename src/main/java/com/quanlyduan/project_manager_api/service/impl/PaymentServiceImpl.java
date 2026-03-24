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
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    
    private final PayOS payOS;

    @Value("${payos.return-url}")
    private String defaultReturnUrl;

    @Value("${payos.cancel-url}")
    private String defaultCancelUrl;

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

        // ========================================================
        // 🚀 2. LOGIC PRORATION: KHẤU TRỪ VÀ QUY ĐỔI NGÀY
        // ========================================================
        LocalDateTime now = LocalDateTime.now();
        BigDecimal remainingValue = BigDecimal.ZERO;
        
        // Nếu khác gói và gói cũ CÒN HẠN
        if (currentSub != null && !currentSub.getPlan().getId().equals(plan.getId()) 
            && currentSub.getCurrentPeriodEnd() != null 
            && currentSub.getCurrentPeriodEnd().isAfter(now)) {
            
            long remainingDays = ChronoUnit.DAYS.between(now, currentSub.getCurrentPeriodEnd());
            if (remainingDays > 0) {
                // Sửa lỗi: Luôn ưu tiên dùng giá NĂM để tính giá 1 ngày cho chuẩn, tránh lỗi Monthly = 0
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

        // Số tiền thực tế phải trả = Giá gốc - Tiền dư
        BigDecimal finalAmountToPay = originalAmount.subtract(remainingValue);
        long orderCode = System.currentTimeMillis(); 
        String transactionCode = String.valueOf(orderCode);
        String returnUrl = request.getReturnUrl() != null ? request.getReturnUrl() : defaultReturnUrl;

        // ========================================================
        // KỊCH BẢN ĐẶC BIỆT: KHÁCH CÒN QUÁ NHIỀU TIỀN DƯ (Thu < 2000đ)
        // ========================================================
        if (finalAmountToPay.compareTo(new BigDecimal(2000)) < 0) {
            
            // 1. Tạo giao dịch thành công ngay lập tức (Thanh toán bằng Tiền dư)
            Transaction transaction = Transaction.builder()
                    .company(company)
                    .plan(plan)
                    .subscription(currentSub) 
                    .transactionCode(transactionCode)
                    .amount(BigDecimal.ZERO) 
                    .currency("VND")
                    .billingCycle(cycle)
                    .paymentMethod("SYSTEM_CREDIT") // Đánh dấu là thanh toán nội bộ
                    .status(TransactionStatus.SUCCESS)
                    .createdBy(user)
                    .build();
            transactionRepository.save(transaction);
            
            // 2. Đóng gói cũ
            if (currentSub != null) currentSub.setStatus(SubscriptionStatus.EXPIRED);
            
            // 3. Tính ngày kết thúc mới
            LocalDateTime newEndDate = (cycle == BillingCycle.YEARLY) ? now.plusYears(1) : now.plusMonths(1);
            
            // 4. NẾU TIỀN DƯ > GIÁ GÓI MỚI -> QUY ĐỔI THÀNH NGÀY TẶNG THÊM
            if (remainingValue.compareTo(originalAmount) > 0) {
                BigDecimal extraValue = remainingValue.subtract(originalAmount);
                BigDecimal newYearlyPrice = plan.getYearlyPrice() != null ? plan.getYearlyPrice() : plan.getMonthlyPrice().multiply(new BigDecimal(12));
                BigDecimal newDailyRate = newYearlyPrice.divide(new BigDecimal(365), 2, RoundingMode.HALF_UP);
                
                if (newDailyRate.compareTo(BigDecimal.ZERO) > 0) {
                    long bonusDays = extraValue.divide(newDailyRate, 0, RoundingMode.DOWN).longValue();
                    newEndDate = newEndDate.plusDays(bonusDays);
                    log.info("Tặng thêm {} ngày sử dụng gói mới từ phần tiền dư thừa.", bonusDays);
                }
            }
            
            // 5. Lưu gói mới
            CompanySubscription newSub = new CompanySubscription();
            newSub.setCompany(company);
            newSub.setPlan(plan);
            newSub.setStatus(SubscriptionStatus.ACTIVE);
            newSub.setCurrentPeriodStart(now);
            newSub.setCurrentPeriodEnd(newEndDate);
            company.getSubscriptions().add(newSub);
            companyRepository.save(company);

            // Bỏ qua PayOS, điều hướng Frontend thẳng về trang Thành công!
            return CheckoutResponse.builder()
                    .transactionCode(transactionCode)
                    .checkoutUrl(returnUrl) 
                    .build();
        }

        // ========================================================
        // 💳 KỊCH BẢN BÌNH THƯỜNG: CẦN GỌI PAYOS THANH TOÁN
        // ========================================================
        Transaction transaction = Transaction.builder()
                .company(company)
                .plan(plan)
                .subscription(currentSub) 
                .transactionCode(transactionCode)
                .amount(finalAmountToPay) // Thu số tiền chênh lệch
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

    @Override
    @Transactional
    public void processWebhook(Webhook webhookBody) {
        try {
            // 1. Chặn các request test (ping) không có chữ ký từ PayOS
            if (webhookBody.getSignature() == null || webhookBody.getSignature().isEmpty()) {
                log.warn("Bỏ qua Webhook do không có chữ ký (signature). Đây là ping test từ PayOS.");
                return; 
            }

            // 2. Xác thực chữ ký
            WebhookData data = payOS.webhooks().verify(webhookBody);
            log.info("Nhận được Webhook từ PayOS. Mã đơn hàng: {}", data.getOrderCode());

            // 3. Xử lý khi giao dịch thành công ("00")
            if ("00".equals(data.getCode())) {
                String transactionCode = String.valueOf(data.getOrderCode());

                Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch: " + transactionCode));

                // 4. Nếu đơn đang PENDING thì cập nhật trạng thái đơn và nâng cấp gói
                if (transaction.getStatus() == TransactionStatus.PENDING) {
                    
                    transaction.setStatus(TransactionStatus.SUCCESS);
                    transactionRepository.save(transaction);
                    log.info("Giao dịch {} thanh toán THÀNH CÔNG. Đã cập nhật trạng thái đơn!", transactionCode);
                    
                    // ==========================================
                    // HỦY CÁC MÃ QR PENDING CŨ CỦA CÔNG TY
                    // ==========================================
                    List<Transaction> oldPendingTransactions = transactionRepository
                            .findByCompanyIdAndStatusAndIdNot(
                                    transaction.getCompany().getId(), 
                                    TransactionStatus.PENDING, 
                                    transaction.getId()
                            );
                            
                    if (!oldPendingTransactions.isEmpty()) {
                        oldPendingTransactions.forEach(tx -> tx.setStatus(TransactionStatus.CANCELLED));
                        transactionRepository.saveAll(oldPendingTransactions);
                        log.info("🚫 Đã tự động hủy {} giao dịch PENDING cũ của Công ty [{}] để tránh thanh toán trùng lặp.", 
                                oldPendingTransactions.size(), transaction.getCompany().getName());
                    }

                    // ==========================================
                    // 🚀 5. LOGIC NÂNG CẤP: LƯU VẾT LỊCH SỬ CHUẨN SAAS
                    // ==========================================
                    Company company = transaction.getCompany();
                    SubscriptionPlan purchasedPlan = transaction.getPlan();
                    BillingCycle cycle = transaction.getBillingCycle();

                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime baseDate = now;

                    // 5.1 Tìm gói cước đang ACTIVE hiện tại của công ty (nếu có)
                    CompanySubscription currentSub = company.getSubscriptions().stream()
                            .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                            .findFirst()
                            .orElse(null);

                    // 5.2 Tính toán ngày tháng và Đóng gói cũ
                    if (currentSub != null) {
                        boolean isSamePlan = currentSub.getPlan().getId().equals(purchasedPlan.getId());
                        boolean isNotExpired = currentSub.getCurrentPeriodEnd() != null && 
                                               currentSub.getCurrentPeriodEnd().isAfter(now);

                        if (isSamePlan && isNotExpired) {
                            // Cùng gói, còn hạn -> Lấy mốc cũ để cộng dồn
                            baseDate = currentSub.getCurrentPeriodEnd();
                        }
                        
                        // ĐÓNG SỔ GÓI CŨ: Chuyển thành EXPIRED
                        currentSub.setStatus(SubscriptionStatus.EXPIRED);
                    }

                    // 5.3 Tính ngày hết hạn cho gói mới
                    LocalDateTime newEndDate;
                    if (cycle == BillingCycle.YEARLY) {
                        newEndDate = baseDate.plusYears(1);
                    } else {
                        newEndDate = baseDate.plusMonths(1);
                    }

                    // 5.4 TẠO GÓI CƯỚC MỚI TINH (Lưu lại lịch sử)
                    CompanySubscription newSubscription = new CompanySubscription();
                    newSubscription.setCompany(company);
                    newSubscription.setPlan(purchasedPlan);
                    newSubscription.setStatus(SubscriptionStatus.ACTIVE);
                    newSubscription.setCurrentPeriodStart(now);
                    newSubscription.setCurrentPeriodEnd(newEndDate);
                    // (Tuỳ chọn) Lưu kỳ thanh toán vào gói nếu bạn có trường này
                    // newSubscription.setCancelAtPeriodEnd(false); 
                    
                    // Thêm bản ghi mới vào danh sách của công ty
                    company.getSubscriptions().add(newSubscription);

                    // 5.5 Lưu toàn bộ thay đổi
                    companyRepository.save(company);

                    log.info("🎉 HOÀN TẤT: Công ty [{}] đã chuyển sang gói [{}] mới. Gói cũ đã được đóng lại. Hết hạn mới: {}", 
                             company.getName(), purchasedPlan.getName(), newEndDate);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý Webhook hoặc Chữ ký không hợp lệ: ", e);
            throw new BadRequestException("Webhook verification failed.");
        }
    }

    // =================================================================================
    // 🔴 API: KHÁCH CHỦ ĐỘNG HỦY ĐƠN HÀNG (TRƯỜNG HỢP A)
    // =================================================================================
    @Override
    @Transactional
    public void cancelPendingTransaction(String transactionCode, Integer companyId, String reason) {
        
        // 1. Tìm giao dịch trong Database
        Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giao dịch: " + transactionCode));

        // 2. Bảo mật: Chặn đứng nếu người dùng cố tình hủy đơn của công ty khác
        if (!transaction.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Access Denied: Bạn không có quyền hủy giao dịch này.");
        }

        // 3. Chỉ cho phép hủy nếu đơn đang ở trạng thái PENDING
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new BadRequestException("Giao dịch này không ở trạng thái chờ thanh toán nên không thể hủy.");
        }

        // 4. Cập nhật Database: Chuyển PENDING -> CANCELLED
        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);

        // 5. Gọi sang PayOS để HỦY MÃ QR TRÊN HỆ THỐNG CỦA HỌ
        try {
            long orderCode = Long.parseLong(transactionCode);
            
            // Báo PayOS hủy đơn. (Tránh việc khách lưu ảnh QR về máy, hôm sau mang ra quét vẫn bị trừ tiền)
            // Lưu ý: Tuỳ thuộc phiên bản PayOS SDK, tham số thứ 2 có thể là String hoặc Object
            payOS.paymentRequests().cancel(orderCode, reason);
            
            log.info("🚫 Đã hủy đơn hàng [{}] thành công trên cả Local và PayOS.", transactionCode);
            
        } catch (Exception e) {
            // Đôi khi mã QR đã tự hết hạn trên PayOS trước đó, API của PayOS sẽ báo lỗi.
            // Chúng ta CATCH lỗi này để KHÔNG quăng lỗi 500 ra Frontend, vì ở Local (DB) chúng ta đã hủy thành công rồi.
            log.warn("Đã hủy đơn [{}] ở Local, nhưng PayOS báo lỗi (có thể QR đã hết hạn từ trước): {}", 
                     transactionCode, e.getMessage());
        }
    }

    // Chạy ngầm định kỳ mỗi 5 phút (300.000 ms)
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cleanupExpiredTransactions() {
        
        // 1. Xác định mốc thời gian: 15 phút trước so với thời điểm hiện tại
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        
        // 2. Lên Database tìm tất cả các đơn PENDING tạo trước mốc 15 phút đó
        List<Transaction> expiredTransactions = transactionRepository
                .findByStatusAndCreatedAtBefore(TransactionStatus.PENDING, fifteenMinutesAgo);

        // 3. Nếu tìm thấy rác thì dọn dẹp
        if (!expiredTransactions.isEmpty()) {
            
            // Chuyển toàn bộ sang trạng thái CANCELLED
            expiredTransactions.forEach(tx -> tx.setStatus(TransactionStatus.CANCELLED));
            
            // Lưu lại vào DB một lượt (Batch Update) cho tối ưu hiệu suất
            transactionRepository.saveAll(expiredTransactions);
            
            log.info("🧹 Cron Job: Đã quét và tự động HỦY {} đơn hàng PENDING do quá hạn 15 phút không thanh toán.", 
                     expiredTransactions.size());
        }
    }

    // =========================================================================
    // API 1: LẤY DANH SÁCH GIAO DỊCH (CÓ PHÂN TRANG & LỌC)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<TransactionListResponse> getTransactionHistory(
            Integer companyId, int page, int size, String status, LocalDateTime startDate, LocalDateTime endDate) {

        // Luôn sắp xếp mới nhất lên đầu
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
    // API 3: XEM CHI TIẾT GIAO DỊCH (KÈM BÓC TÁCH DÒNG TIỀN & TRẠNG THÁI GÓI)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransactionDetail(String transactionCode, Integer companyId) {
        
        // 1. Lấy giao dịch (Bảo mật: Phải đúng của công ty đó)
        Transaction tx = transactionRepository.findByTransactionCodeAndCompanyId(transactionCode, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch hoặc bạn không có quyền xem."));

        // 2. Tính toán lại logic Proration (Khấu trừ)
        // Giá gốc phụ thuộc vào việc khách mua tháng hay mua năm ở giao dịch đó
        BigDecimal originalPrice = tx.getBillingCycle() == BillingCycle.YEARLY 
                                   ? tx.getPlan().getYearlyPrice() 
                                   : tx.getPlan().getMonthlyPrice();
                                   
        // Số tiền được giảm (Do dư gói cũ) = Giá gốc - Số tiền thực thu (Đảm bảo >= 0)
        BigDecimal deducted = originalPrice.subtract(tx.getAmount());
        if (deducted.compareTo(BigDecimal.ZERO) < 0) deducted = BigDecimal.ZERO;

        // 3. Lấy Tình trạng sử dụng hiện tại của Công ty
        Company company = tx.getCompany();
        CompanySubscription activeSub = company.getSubscriptions().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE || s.getStatus() == SubscriptionStatus.PAST_DUE)
                .findFirst()
                .orElse(null);

        // 4. Build dữ liệu trả về
        TransactionDetailResponse.TransactionDetailResponseBuilder builder = TransactionDetailResponse.builder()
                // Cơ bản
                .transactionCode(tx.getTransactionCode())
                .createdAt(tx.getCreatedAt())
                .paidAt(tx.getPaidAt())
                .status(tx.getStatus().toString())
                .paymentMethod(tx.getPaymentMethod())
                // Dòng tiền
                .originalPrice(originalPrice)
                .deductedAmount(deducted)
                .finalPaidAmount(tx.getAmount())
                // Đối soát
                .gatewayReferenceCode(tx.getGatewayTransactionId());

        // Nếu công ty đang có gói cước, đính kèm thông tin sử dụng vào
        if (activeSub != null) {
            builder.currentPlanName(activeSub.getPlan().getName())
                   .subscriptionStatus(activeSub.getStatus().toString())
                   .currentPeriodStart(activeSub.getCurrentPeriodStart())
                   .currentPeriodEnd(activeSub.getCurrentPeriodEnd())
                   .isPendingCancel(Boolean.TRUE.equals(activeSub.getCancelAtPeriodEnd()));
        }

        return builder.build();
    }
}