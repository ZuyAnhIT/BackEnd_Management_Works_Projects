package com.quanlyduan.project_manager_api.scheduler;

import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.SubscriptionPlan;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus; // Đảm bảo đúng đường dẫn Enum của bạn
import com.quanlyduan.project_manager_api.repository.CompanySubscriptionRepository;
import com.quanlyduan.project_manager_api.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j // Dùng để in log ra Console cho dễ theo dõi
public class SubscriptionScheduler {

    private final CompanySubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;

    /**
     * Tạm thời để Cron chạy MỖI PHÚT (0 * * * * ?) để test.
     * Sau khi test xong, hãy đổi thành: "0 0 0 * * ?" (Chạy lúc 00:00:00 mỗi ngày)
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void processExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        log.info("⏳ [CRONJOB] Bắt đầu rà soát các gói cước hết hạn lúc: {}", now);

        // =========================================================================
        // LOGIC 1: ÂN HẠN (ACTIVE -> PAST_DUE)
        // Tìm gói đang ACTIVE nhưng đã vượt quá hạn (currentPeriodEnd < NOW)
        // =========================================================================
        List<CompanySubscription> expiredActiveSubs = subscriptionRepository
                .findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus.ACTIVE, now);

        for (CompanySubscription sub : expiredActiveSubs) {
            sub.setStatus(SubscriptionStatus.PAST_DUE);
            log.info("⚠️ [ÂN HẠN] Công ty ID {} đã hết hạn gói {}. Chuyển sang PAST_DUE.", 
                     sub.getCompany().getId(), sub.getPlan().getPlanCode());
        }
        subscriptionRepository.saveAll(expiredActiveSubs);

        // =========================================================================
        // LOGIC 2: CẮT GÓI & HẠ CẤP (PAST_DUE -> VỀ GÓI FREE)
        // Tìm gói PAST_DUE đã quá 3 ngày ân hạn (currentPeriodEnd < NOW - 3 ngày)
        // =========================================================================
        LocalDateTime gracePeriodLimit = now.minusDays(3);
        List<CompanySubscription> subsToDowngrade = subscriptionRepository
                .findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus.PAST_DUE, gracePeriodLimit);

        if (!subsToDowngrade.isEmpty()) {
            // Lấy Gói FREE từ Database
            SubscriptionPlan freePlan = planRepository.findByPlanCodeIgnoreCase("FREE")
                    .orElseThrow(() -> new RuntimeException("CRITICAL ERROR: Không tìm thấy gói FREE trong hệ thống!"));

            for (CompanySubscription sub : subsToDowngrade) {
                String oldPlan = sub.getPlan().getPlanCode();
                
                // Cập nhật lại bản ghi hiện tại thành gói FREE
                sub.setPlan(freePlan);
                sub.setStatus(SubscriptionStatus.ACTIVE); // Trở lại hoạt động bình thường trên gói FREE
                sub.setCurrentPeriodStart(now);
                
                // Gói FREE thường không có ngày hết hạn, hoặc set 100 năm sau
                // Giả sử logic của bạn là FREE thì currentPeriodEnd = null
                sub.setCurrentPeriodEnd(null); 
                
                log.info("🛑 [HẠ CẤP] Công ty ID {} đã bị giáng từ gói {} xuống gói FREE do quá hạn thanh toán.", 
                         sub.getCompany().getId(), oldPlan);
            }
            subscriptionRepository.saveAll(subsToDowngrade);
        }
        
        log.info("✅ [CRONJOB] Hoàn tất rà soát.");
    }
}