package com.quanlyduan.project_manager_api.scheduler;

import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.SubscriptionPlan;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
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
@Slf4j
public class SubscriptionScheduler {

    private final CompanySubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final CompanyRepository companyRepository; // Thêm CompanyRepo để cập nhật danh sách 1-N

    /**
     * Cron chạy MỖI PHÚT (0 * * * * ?) để test.
     * Đưa lên Production đổi thành: "0 0 0 * * ?" (Chạy lúc 00:00:00 mỗi đêm)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        log.info("⏳ [CRONJOB] Bắt đầu rà soát các gói cước hết hạn lúc: {}", now);

        // Lấy sẵn gói FREE để dùng chung cho các công ty bị hạ cấp
        SubscriptionPlan freePlan = planRepository.findByPlanCodeIgnoreCase("FREE")
                .orElseThrow(() -> new RuntimeException("CRITICAL ERROR: Không tìm thấy gói FREE trong hệ thống!"));

        // =========================================================================
        // LOGIC 1: XỬ LÝ CÁC GÓI VỪA HẾT HẠN (Đang ACTIVE nhưng currentPeriodEnd < NOW)
        // =========================================================================
        List<CompanySubscription> expiredActiveSubs = subscriptionRepository
                .findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus.ACTIVE, now);

        for (CompanySubscription sub : expiredActiveSubs) {
            Company company = sub.getCompany();

            // KỊCH BẢN 1A: Khách đã chủ động bấm HỦY từ trước (Trường hợp B) -> CẮT NGAY VỀ FREE
            if (Boolean.TRUE.equals(sub.getCancelAtPeriodEnd())) {
                
                sub.setStatus(SubscriptionStatus.EXPIRED); // Đóng sổ gói cũ
                
                // Mở sổ gói FREE mới (BẢO TOÀN LỊCH SỬ)
                CompanySubscription freeSub = createFreeSubscription(company, freePlan, now);
                company.getSubscriptions().add(freeSub);
                
                log.info("[HỦY CHỦ ĐỘNG] Công ty [{}] kết thúc gói [{}] theo yêu cầu. Đã chuyển về FREE.", 
                         company.getId(), sub.getPlan().getPlanCode());
            } 
            // KỊCH BẢN 1B: Khách quên thanh toán -> CHO ÂN HẠN 3 NGÀY (PAST_DUE)
            else {
                sub.setStatus(SubscriptionStatus.PAST_DUE);
                log.info("[ÂN HẠN] Công ty [{}] hết hạn gói [{}]. Chuyển sang PAST_DUE chờ thanh toán.", 
                         company.getId(), sub.getPlan().getPlanCode());
            }
        }
        
        // Lưu lại các thay đổi của Kịch bản 1
        if (!expiredActiveSubs.isEmpty()) {
            subscriptionRepository.saveAll(expiredActiveSubs);
            // Các công ty bị đổi về Free sẽ được Hibernate cascade save thông qua liên kết (nếu cần thiết có thể gọi companyRepository.saveAll)
        }

        // =========================================================================
        // LOGIC 2: HẾT THỜI GIAN ÂN HẠN (Đang PAST_DUE và đã quá 3 ngày) -> CẮT VỀ FREE
        // =========================================================================
        LocalDateTime gracePeriodLimit = now.minusDays(3);
        List<CompanySubscription> subsToDowngrade = subscriptionRepository
                .findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus.PAST_DUE, gracePeriodLimit);

        for (CompanySubscription sub : subsToDowngrade) {
            Company company = sub.getCompany();
            
            sub.setStatus(SubscriptionStatus.EXPIRED); // Đóng sổ gói nợ cước
            
            // Mở sổ gói FREE mới (BẢO TOÀN LỊCH SỬ)
            CompanySubscription freeSub = createFreeSubscription(company, freePlan, now);
            company.getSubscriptions().add(freeSub);
            
            log.info("[HẠ CẤP] Công ty [{}] hết 3 ngày ân hạn gói [{}]. Đã tự động giáng xuống FREE.", 
                     company.getId(), sub.getPlan().getPlanCode());
        }

        if (!subsToDowngrade.isEmpty()) {
            subscriptionRepository.saveAll(subsToDowngrade);
        }

        log.info("[CRONJOB] Hoàn tất rà soát.");
    }

    // =========================================================================
    // HÀM HELPER: Tạo bản ghi gói FREE mới tinh (Tránh ghi đè làm mất lịch sử)
    // =========================================================================
    private CompanySubscription createFreeSubscription(Company company, SubscriptionPlan freePlan, LocalDateTime startTime) {
        CompanySubscription freeSub = new CompanySubscription();
        freeSub.setCompany(company);
        freeSub.setPlan(freePlan);
        freeSub.setStatus(SubscriptionStatus.ACTIVE);
        freeSub.setCurrentPeriodStart(startTime);
        freeSub.setCurrentPeriodEnd(null); // Gói FREE dùng vĩnh viễn không có ngày hết hạn
        freeSub.setCancelAtPeriodEnd(false);
        return freeSub;
    }
}