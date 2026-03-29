package com.quanlyduan.project_manager_api.scheduler;

import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.SubscriptionPlan;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.repository.CompanySubscriptionRepository;
import com.quanlyduan.project_manager_api.repository.SubscriptionPlanRepository;
import com.quanlyduan.project_manager_api.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class SubscriptionScheduler {

    // Khai bao cac hang so cau hinh he thong
    private static final String CRON_MIDNIGHT = "0 0 0 * * ?";
    private static final String CRON_EIGHT_AM = "0 0 8 * * ?";
    private static final String FREE_PLAN_CODE = "FREE";
    private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    private static final String FRONTEND_BILLING_URL = "http://localhost:3000/admin/company/billing";
    private static final int GRACE_PERIOD_DAYS = 3;
    private static final int WARNING_BEFORE_DAYS = 3;

    // Khai bao template email
    private static final String EMAIL_SUBJECT_FORMAT = "Warning: Your subscription for %s is expiring soon!";
    private static final String EMAIL_CONTENT_TEMPLATE = 
        "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;\">" +
        "  <div style=\"text-align: center; padding-bottom: 20px;\">" +
        "    <h2 style=\"color: #e67e22; margin: 0;\">SUBSCRIPTION EXPIRATION NOTICE</h2>" +
        "  </div>" +
        "  <p>Hello <b>%s</b>,</p>" +
        "  <p>Your <b>%s</b> plan is set to expire on <b>%s</b> (in 3 days).</p>" +
        "  <div style=\"background-color: #fdf2e9; padding: 15px; border-left: 4px solid #e67e22; margin: 20px 0;\">" +
        "    <p style=\"margin: 0; color: #d35400;\"><b>Important:</b> If not renewed, your account will be downgraded to the FREE plan. Some resources exceeding the FREE limits might be temporarily locked.</p>" +
        "  </div>" +
        "  <p>Please renew your subscription to ensure uninterrupted service.</p>" +
        "  <div style=\"text-align: center; margin-top: 30px;\">" +
        "    <a href=\"%s\" style=\"background-color: #e67e22; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;\">Renew Now</a>" +
        "  </div>" +
        "  <hr style=\"border: none; border-top: 1px solid #e0e0e0; margin-top: 30px;\">" +
        "  <p style=\"font-size: 12px; color: #7f8c8d; text-align: center;\">Thank you for using our service.</p>" +
        "</div>";

    private final CompanySubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final EmailService emailService;

    // Khoi tao constructor thu cong
    public SubscriptionScheduler(CompanySubscriptionRepository subscriptionRepository,
                                 SubscriptionPlanRepository planRepository,
                                 EmailService emailService) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.emailService = emailService;
    }

    /**
     * Quet va xu ly cac goi cuoc da het han vao luc 00:00 hang ngay.
     */
    @Scheduled(cron = CRON_MIDNIGHT)
    @Transactional
    public void processExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Starting subscription expiration check at {}", now);

        SubscriptionPlan freePlan = planRepository.findByPlanCodeIgnoreCase(FREE_PLAN_CODE)
                .orElseThrow(() -> new ResourceNotFoundException("Critical error: FREE plan not found."));

        // Xu ly ha cap cac goi ACTIVE da den han
        handleRecentlyExpiredActiveSubs(now, freePlan);

        // Xu ly cac goi PAST_DUE da het thoi gian an han
        handleExpiredPastDueSubs(now, freePlan);

        log.info("Subscription expiration check completed.");
    }

    /**
     * Gui email canh bao het han cho khach hang vao 08:00 hang ngay.
     */
    @Scheduled(cron = CRON_EIGHT_AM)
    @Transactional(readOnly = true)
    public void sendExpirationWarnings() {
        log.info("Starting expiration warning email scan...");

        LocalDateTime startTarget = LocalDate.now().plusDays(WARNING_BEFORE_DAYS).atStartOfDay();
        LocalDateTime endTarget = startTarget.plusDays(1).minusNanos(1);

        List<CompanySubscription> expiringSubs = subscriptionRepository
                .findByStatusAndCurrentPeriodEndBetween(SubscriptionStatus.ACTIVE, startTarget, endTarget);

        if (expiringSubs.isEmpty()) {
            log.info("No subscriptions expiring in {} days.", WARNING_BEFORE_DAYS);
            return;
        }

        processWarningEmailDispatch(expiringSubs);
        log.info("Expiration warning dispatch completed.");
    }

    // --- Private Helper Methods ---

    /**
     * Xu ly cac goi cuoc vua den ngay het han.
     */
    private void handleRecentlyExpiredActiveSubs(LocalDateTime now, SubscriptionPlan freePlan) {
        List<CompanySubscription> expiredActiveSubs = subscriptionRepository
                .findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus.ACTIVE, now);

        for (CompanySubscription sub : expiredActiveSubs) {
            Company company = sub.getCompany();

            // Kich ban: Khach chu dong huy -> Chuyen ve FREE ngay
            if (Boolean.TRUE.equals(sub.getCancelAtPeriodEnd())) {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                company.getSubscriptions().add(createFreeSubscriptionMapping(company, freePlan, now));
                log.info("Company {} downgraded to FREE due to active cancellation.", company.getId());
            } 
            // Kich ban: Quen thanh toan -> Cho vao trang thai no cuoc
            else {
                sub.setStatus(SubscriptionStatus.PAST_DUE);
                log.info("Company {} subscription set to PAST_DUE for grace period.", company.getId());
            }
        }
        
        if (!expiredActiveSubs.isEmpty()) {
            subscriptionRepository.saveAll(expiredActiveSubs);
        }
    }

    /**
     * Xu ly ha cap cac goi cuoc da qua thoi gian an han.
     */
    private void handleExpiredPastDueSubs(LocalDateTime now, SubscriptionPlan freePlan) {
        LocalDateTime graceLimit = now.minusDays(GRACE_PERIOD_DAYS);
        List<CompanySubscription> subsToDowngrade = subscriptionRepository
                .findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus.PAST_DUE, graceLimit);

        for (CompanySubscription sub : subsToDowngrade) {
            Company company = sub.getCompany();
            sub.setStatus(SubscriptionStatus.EXPIRED);
            company.getSubscriptions().add(createFreeSubscriptionMapping(company, freePlan, now));
            log.info("Company {} grace period exceeded. Downgraded to FREE.", company.getId());
        }

        if (!subsToDowngrade.isEmpty()) {
            subscriptionRepository.saveAll(subsToDowngrade);
        }
    }

    /**
     * Thuc hien gui email canh bao theo danh sach.
     */
    private void processWarningEmailDispatch(List<CompanySubscription> expiringSubs) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

        for (CompanySubscription sub : expiringSubs) {
            if (Boolean.TRUE.equals(sub.getCancelAtPeriodEnd())) {
                continue;
            }

            Company company = sub.getCompany();
            String expireDate = sub.getCurrentPeriodEnd().format(formatter);
            String planName = sub.getPlan().getName();

            String subject = String.format(EMAIL_SUBJECT_FORMAT, planName);
            String body = String.format(EMAIL_CONTENT_TEMPLATE, 
                    company.getName(), planName, expireDate, FRONTEND_BILLING_URL);

            emailService.sendEmail(company.getEmail(), subject, body);
            log.info("Warning email sent to company: {}", company.getName());
        }
    }

    /**
     * Mapping thong tin de tao moi mot goi cuoc mien phi.
     */
    private CompanySubscription createFreeSubscriptionMapping(Company company, SubscriptionPlan freePlan, LocalDateTime startTime) {
        CompanySubscription freeSub = new CompanySubscription();
        freeSub.setCompany(company);
        freeSub.setPlan(freePlan);
        freeSub.setStatus(SubscriptionStatus.ACTIVE);
        freeSub.setCurrentPeriodStart(startTime);
        freeSub.setCurrentPeriodEnd(null);
        freeSub.setCancelAtPeriodEnd(false);
        return freeSub;
    }
}