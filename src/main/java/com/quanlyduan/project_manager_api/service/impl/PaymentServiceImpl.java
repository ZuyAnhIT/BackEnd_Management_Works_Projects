package com.quanlyduan.project_manager_api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.request.CheckoutRequest;
import com.quanlyduan.project_manager_api.dto.response.CheckoutResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.TransactionDetailResponse;
import com.quanlyduan.project_manager_api.dto.response.TransactionListResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.SubscriptionPlan;
import com.quanlyduan.project_manager_api.model.Transaction;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.BillingCycle;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TransactionStatus;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.SubscriptionPlanRepository;
import com.quanlyduan.project_manager_api.repository.TransactionRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.PaymentService;

import lombok.extern.slf4j.Slf4j;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ERROR_COMPANY_NOT_FOUND = "Company not found.";
    public static final String ERROR_USER_NOT_FOUND = "User not found.";
    public static final String ERROR_PLAN_NOT_FOUND = "Subscription plan not found.";
    public static final String ERROR_NO_PAYMENT_REQUIRED = "This plan does not require payment.";
    public static final String ERROR_CREATE_LINK_FAILED = "Cannot create payment link at this time.";
    public static final String ERROR_TRANSACTION_NOT_FOUND = "Transaction not found: ";
    public static final String ERROR_TRANSACTION_NOT_FOUND_OR_DENIED = "Transaction not found or access denied.";
    public static final String ERROR_WEBHOOK_VERIFICATION_FAILED = "Webhook verification failed.";
    public static final String ERROR_CANCEL_DENIED = "Access Denied: You do not have permission to cancel this transaction.";
    public static final String ERROR_NOT_PENDING = "This transaction is not pending and cannot be cancelled.";

    public static final String CURRENCY_VND = "VND";
    public static final String METHOD_SYSTEM_CREDIT = "SYSTEM_CREDIT";
    public static final String METHOD_PAYOS = "PAYOS";
    public static final String STATUS_ALL = "ALL";
    public static final String WEBHOOK_SUCCESS_CODE = "00";

    public static final String DESC_UPGRADE_PLAN = "Upgrade plan ";
    public static final String LOCALE_VI = "vi";
    public static final String LOCALE_VN = "VN";
    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String TEXT_UNLIMITED = "Unlimited";
    public static final String FIELD_CREATED_AT = "createdAt";

    public static final int MONTHS_IN_YEAR = 12;
    public static final int DAYS_IN_YEAR = 365;
    public static final int MINIMUM_PAYMENT_AMOUNT = 2000;
    public static final int MAX_DESC_LENGTH = 25;
    public static final int CLEANUP_CRON_RATE_MS = 300000;
    public static final int PENDING_EXPIRATION_MINUTES = 15;

    // Mau email bien lai
    public static final String EMAIL_RECEIPT_SUBJECT = "Payment Receipt: Upgrade to plan %s successful";
    public static final String EMAIL_RECEIPT_TEMPLATE = "<div style=\"font-family: Arial, sans-serif; max-width: 650px; margin: 0 auto; border: 1px solid #e1e5eb; border-radius: 8px; overflow: hidden;\">" +
            "  <div style=\"background-color: #2c3e50; padding: 25px; text-align: center; color: white;\">" +
            "    <h1 style=\"margin: 0; font-size: 24px;\">PAYMENT RECEIPT</h1>" +
            "    <p style=\"margin: 10px 0 0 0; font-size: 14px; opacity: 0.9;\">Thank you for using our service!</p>" +
            "  </div>" +
            "  <div style=\"padding: 30px;\">" +
            "    <p>Hello <b>%s</b>,</p>" + 
            "    <p>We confirm that we have received your payment for the project management system at company <b>%s</b>. Your subscription plan has been successfully activated.</p>" +
            "    <table style=\"width: 100%%; border-collapse: collapse; margin: 25px 0; background-color: #f8f9fa; border-radius: 5px;\">" +
            "      <tr>" +
            "        <td style=\"padding: 15px; border-bottom: 1px solid #e1e5eb;\"><b>Transaction Code:</b></td>" +
            "        <td style=\"padding: 15px; text-align: right; border-bottom: 1px solid #e1e5eb; font-family: monospace;\">#%s</td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td style=\"padding: 15px; border-bottom: 1px solid #e1e5eb;\"><b>Payment Date:</b></td>" +
            "        <td style=\"padding: 15px; text-align: right; border-bottom: 1px solid #e1e5eb;\">%s</td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td style=\"padding: 15px; border-bottom: 1px solid #e1e5eb;\"><b>Service Plan:</b></td>" +
            "        <td style=\"padding: 15px; text-align: right; border-bottom: 1px solid #e1e5eb; color: #3498db; font-weight: bold;\">%s</td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td style=\"padding: 15px; border-bottom: 1px solid #e1e5eb;\"><b>Next Billing Date:</b></td>" +
            "        <td style=\"padding: 15px; text-align: right; border-bottom: 1px solid #e1e5eb;\">%s</td>" +
            "      </tr>" +
            "      <tr>" +
            "        <td style=\"padding: 15px; font-size: 18px;\"><b>TOTAL PAID:</b></td>" +
            "        <td style=\"padding: 15px; text-align: right; font-size: 18px; color: #27ae60; font-weight: bold;\">%s</td>" +
            "      </tr>" +
            "    </table>" +
            "    <p style=\"color: #7f8c8d; font-size: 13px; line-height: 1.5;\">" +
            "      * This is an electronic receipt automatically generated from the system. You can review your entire transaction history in the Company Settings section." +
            "    </p>" +
            "  </div>" +
            "  <div style=\"background-color: #f4f6f8; padding: 15px; text-align: center; color: #7f8c8d; font-size: 12px;\">" +
            "    <p style=\"margin: 0;\">© 2026 Worknet - Project Management System</p>" +
            "  </div>" +
            "</div>";

    // Khai bao cac bien phu thuoc
    private final TransactionRepository transactionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PayOS payOS;
    private final EmailService emailService;

    @Value("${payos.return-url}")
    private String defaultReturnUrl;

    @Value("${payos.cancel-url}")
    private String defaultCancelUrl;

    // Constructor thay the cho @RequiredArgsConstructor
    public PaymentServiceImpl(TransactionRepository transactionRepository,
                              SubscriptionPlanRepository planRepository,
                              CompanyRepository companyRepository,
                              UserRepository userRepository,
                              PayOS payOS,
                              EmailService emailService) {
        this.transactionRepository = transactionRepository;
        this.planRepository = planRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.payOS = payOS;
        this.emailService = emailService;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional
    public CheckoutResponse createPaymentLink(Integer companyId, Integer userId, CheckoutRequest request) {
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND));
        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PLAN_NOT_FOUND));

        BillingCycle cycle = BillingCycle.valueOf(request.getBillingCycle().toUpperCase());

        // Tinh gia goc cua goi dich vu muon mua
        BigDecimal originalAmount = (cycle == BillingCycle.YEARLY) ? plan.getYearlyPrice() : plan.getMonthlyPrice();
        if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            if (cycle == BillingCycle.YEARLY && plan.getMonthlyPrice() != null) {
                originalAmount = plan.getMonthlyPrice().multiply(new BigDecimal(MONTHS_IN_YEAR));
            } else {
                throw new BadRequestException(ERROR_NO_PAYMENT_REQUIRED);
            }
        }

        CompanySubscription currentSub = company.getSubscriptions().stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        // Logic tinh toan tien khau tru dua tren thoi gian con lai
        LocalDateTime now = LocalDateTime.now();
        BigDecimal remainingValue = BigDecimal.ZERO;
        
        if (currentSub != null && !currentSub.getPlan().getId().equals(plan.getId()) 
            && currentSub.getCurrentPeriodEnd() != null 
            && currentSub.getCurrentPeriodEnd().isAfter(now)) {
            
            long remainingDays = ChronoUnit.DAYS.between(now, currentSub.getCurrentPeriodEnd());
            if (remainingDays > 0) {
                BigDecimal oldYearlyPrice = currentSub.getPlan().getYearlyPrice();
                if (oldYearlyPrice == null || oldYearlyPrice.compareTo(BigDecimal.ZERO) == 0) {
                    oldYearlyPrice = currentSub.getPlan().getMonthlyPrice().multiply(new BigDecimal(MONTHS_IN_YEAR));
                }
                
                BigDecimal dailyRate = oldYearlyPrice.divide(new BigDecimal(DAYS_IN_YEAR), 2, RoundingMode.HALF_UP);
                remainingValue = dailyRate.multiply(new BigDecimal(remainingDays));
                
                log.info("Plan changed: Company [{}] has {} remaining days of plan [{}]. Value: {} VND", 
                        company.getName(), remainingDays, currentSub.getPlan().getName(), remainingValue);
            }
        }

        BigDecimal finalAmountToPay = originalAmount.subtract(remainingValue);
        long orderCode = System.currentTimeMillis(); 
        String transactionCode = String.valueOf(orderCode);
        String returnUrl = request.getReturnUrl() != null ? request.getReturnUrl() : defaultReturnUrl;

        // Xu ly kich ban chuyen goi khi khach hang con so du lon hon hoac gan bang gia tri goi moi
        if (finalAmountToPay.compareTo(new BigDecimal(MINIMUM_PAYMENT_AMOUNT)) < 0) {
            Transaction transaction = Transaction.builder()
                    .company(company)
                    .plan(plan)
                    .subscription(currentSub) 
                    .transactionCode(transactionCode)
                    .amount(BigDecimal.ZERO) 
                    .currency(CURRENCY_VND)
                    .billingCycle(cycle)
                    .paymentMethod(METHOD_SYSTEM_CREDIT) 
                    .status(TransactionStatus.SUCCESS)
                    .createdBy(user)
                    .build();
            transactionRepository.save(transaction);
            
            if (currentSub != null) {
                currentSub.setStatus(SubscriptionStatus.EXPIRED);
            }
            
            LocalDateTime newEndDate = (cycle == BillingCycle.YEARLY) ? now.plusYears(1) : now.plusMonths(1);
            
            if (remainingValue.compareTo(originalAmount) > 0) {
                BigDecimal extraValue = remainingValue.subtract(originalAmount);
                BigDecimal newYearlyPrice = plan.getYearlyPrice() != null ? plan.getYearlyPrice() : plan.getMonthlyPrice().multiply(new BigDecimal(MONTHS_IN_YEAR));
                BigDecimal newDailyRate = newYearlyPrice.divide(new BigDecimal(DAYS_IN_YEAR), 2, RoundingMode.HALF_UP);
                
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

            sendReceiptEmail(transaction, company, newSub);

            return CheckoutResponse.builder()
                    .transactionCode(transactionCode)
                    .checkoutUrl(returnUrl) 
                    .build();
        }

        // Xu ly tao yeu cau thanh toan qua cong thanh toan
        Transaction transaction = Transaction.builder()
                .company(company)
                .plan(plan)
                .subscription(currentSub) 
                .transactionCode(transactionCode)
                .amount(finalAmountToPay) 
                .currency(CURRENCY_VND)
                .billingCycle(cycle)
                .paymentMethod(METHOD_PAYOS)
                .status(TransactionStatus.PENDING)
                .createdBy(user)
                .build();
        transactionRepository.save(transaction);

        try {
            String cancelUrl = request.getCancelUrl() != null ? request.getCancelUrl() : defaultCancelUrl;
            String description = DESC_UPGRADE_PLAN + plan.getName();
            if (description.length() > MAX_DESC_LENGTH) {
                description = description.substring(0, MAX_DESC_LENGTH);
            }

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
            log.error("Error calling PayOS API: ", e); 
            throw new BadRequestException(ERROR_CREATE_LINK_FAILED);
        }
    }

    @Override
    @Transactional
    public void processWebhook(Webhook webhookBody) {
        try {
            if (webhookBody.getSignature() == null || webhookBody.getSignature().isEmpty()) {
                log.warn("Ignored Webhook due to missing signature.");
                return; 
            }

            WebhookData data = payOS.webhooks().verify(webhookBody);
            log.info("Received Webhook from PayOS. Order code: {}", data.getOrderCode());

            if (WEBHOOK_SUCCESS_CODE.equals(data.getCode())) {
                String transactionCode = String.valueOf(data.getOrderCode());

                Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
                        .orElseThrow(() -> new ResourceNotFoundException(ERROR_TRANSACTION_NOT_FOUND + transactionCode));

                if (transaction.getStatus() == TransactionStatus.PENDING) {
                    
                    transaction.setStatus(TransactionStatus.SUCCESS);
                    transactionRepository.save(transaction);
                    
                    // Huy cac giao dich dang cho khac cua cung cong ty de tranh trung lap
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

                    // Thuc hien nang cap goi cước sau khi thanh toan thanh cong
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

                    log.info("Completed: Company [{}] switched to new plan [{}].", company.getName(), purchasedPlan.getName());

                    // Gui email xac nhan thanh toan va cung cap bien lai
                    sendReceiptEmail(transaction, company, newSubscription);
                }
            }
        } catch (Exception e) {
            log.error("Error processing Webhook or invalid signature: ", e);
            throw new BadRequestException(ERROR_WEBHOOK_VERIFICATION_FAILED);
        }
    }

    @Override
    @Transactional
    public void cancelPendingTransaction(String transactionCode, Integer companyId, String reason) {
        Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TRANSACTION_NOT_FOUND + transactionCode));

        if (!transaction.getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_CANCEL_DENIED);
        }

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new BadRequestException(ERROR_NOT_PENDING);
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);

        try {
            payOS.paymentRequests().cancel(Long.parseLong(transactionCode), reason);
        } catch (Exception e) {
            log.warn("Cancelled transaction [{}] locally, but PayOS reported error: {}", transactionCode, e.getMessage());
        }
    }

    @Scheduled(fixedRate = CLEANUP_CRON_RATE_MS)
    @Transactional
    public void cleanupExpiredTransactions() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(PENDING_EXPIRATION_MINUTES);
        List<Transaction> expiredTransactions = transactionRepository
                .findByStatusAndCreatedAtBefore(TransactionStatus.PENDING, expirationTime);

        if (!expiredTransactions.isEmpty()) {
            expiredTransactions.forEach(tx -> tx.setStatus(TransactionStatus.CANCELLED));
            transactionRepository.saveAll(expiredTransactions);
            log.info("Cron Job: Scanned and automatically cancelled {} expired PENDING transactions.", expiredTransactions.size());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<TransactionListResponse> getTransactionHistory(
            Integer companyId, int page, int size, String status, LocalDateTime startDate, LocalDateTime endDate) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(FIELD_CREATED_AT).descending());
        TransactionStatus txStatus = (status != null && !status.equalsIgnoreCase(STATUS_ALL)) 
                                     ? TransactionStatus.valueOf(status.toUpperCase()) : null;

        Page<Transaction> transactions = transactionRepository.filterTransactions(
                companyId, txStatus, startDate, endDate, pageable);

        Page<TransactionListResponse> dtoPage = transactions.map(this::buildTransactionListResponse);

        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransactionDetail(String transactionCode, Integer companyId) {
        Transaction tx = transactionRepository.findByTransactionCodeAndCompanyId(transactionCode, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TRANSACTION_NOT_FOUND_OR_DENIED));

        BigDecimal originalPrice = tx.getBillingCycle() == BillingCycle.YEARLY 
                                   ? tx.getPlan().getYearlyPrice() 
                                   : tx.getPlan().getMonthlyPrice();
                                   
        BigDecimal deducted = originalPrice.subtract(tx.getAmount());
        if (deducted.compareTo(BigDecimal.ZERO) < 0) {
            deducted = BigDecimal.ZERO;
        }

        Company company = tx.getCompany();
        CompanySubscription activeSub = company.getSubscriptions().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE || s.getStatus() == SubscriptionStatus.PAST_DUE)
                .findFirst()
                .orElse(null);

        return buildTransactionDetailResponse(tx, originalPrice, deducted, activeSub);
    }

    // --- CAC HAM PRIVATE HO TRO NGHIEP VU ---

    private void sendReceiptEmail(Transaction tx, Company company, CompanySubscription sub) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String paymentDate = tx.getCreatedAt().format(dateFormatter);
        String nextBillingDate = sub.getCurrentPeriodEnd() != null 
                ? sub.getCurrentPeriodEnd().format(dateFormatter) 
                : TEXT_UNLIMITED;
                
        NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale(LOCALE_VI, LOCALE_VN));
        String formattedAmount = currencyFormatter.format(tx.getAmount()) + " " + CURRENCY_VND;

        String subject = String.format(EMAIL_RECEIPT_SUBJECT, tx.getPlan().getName());
        User payer = tx.getCreatedBy();

        String emailBody = String.format(EMAIL_RECEIPT_TEMPLATE,
            payer.getFullName(),
            company.getName(), 
            tx.getTransactionCode(),
            paymentDate,
            tx.getPlan().getName(),
            nextBillingDate,
            formattedAmount
        );

        emailService.sendEmail(payer.getEmail(), subject, emailBody);
        log.info("Sent receipt email for transaction {} to payer: {}", tx.getTransactionCode(), payer.getEmail());
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private TransactionListResponse buildTransactionListResponse(Transaction tx) {
        return TransactionListResponse.builder()
                .transactionCode(tx.getTransactionCode())
                .planName(tx.getPlan().getName())
                .amount(tx.getAmount())
                .status(tx.getStatus().toString())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    private TransactionDetailResponse buildTransactionDetailResponse(Transaction tx, BigDecimal originalPrice, BigDecimal deducted, CompanySubscription activeSub) {
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
}