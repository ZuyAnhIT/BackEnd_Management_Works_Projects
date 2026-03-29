package com.quanlyduan.project_manager_api.scheduler;

import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.SubscriptionPlan;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.CompanySubscriptionRepository;
import com.quanlyduan.project_manager_api.repository.SubscriptionPlanRepository;
import com.quanlyduan.project_manager_api.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final CompanySubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final CompanyRepository companyRepository; // Thêm CompanyRepo để cập nhật danh sách 1-N
    private final EmailService emailService;

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

    // =========================================================================
    // ✉️ CRON JOB 2: GỬI EMAIL CẢNH BÁO SẮP HẾT HẠN (CHẠY LÚC 8H SÁNG)
    // =========================================================================
    @Scheduled(cron = "0 0 8 * * ?") // 08:00:00 mỗi ngày
    @Transactional(readOnly = true)
    public void sendExpirationWarnings() {
        log.info("⏰ [CRONJOB] Bắt đầu quét các gói cước sắp hết hạn để gửi Email cảnh báo...");

        // 1. Xác định khung thời gian: Trọn vẹn Ngày thứ 3 kể từ hôm nay
        LocalDateTime startOfTargetDay = LocalDate.now().plusDays(3).atStartOfDay(); // 00:00:00 của 3 ngày sau
        LocalDateTime endOfTargetDay = startOfTargetDay.plusDays(1).minusNanos(1);   // 23:59:59 của 3 ngày sau

        // 2. Tìm các gói ACTIVE sẽ hết hạn trong khung giờ đó
        List<CompanySubscription> expiringSubs = subscriptionRepository
                .findByStatusAndCurrentPeriodEndBetween(SubscriptionStatus.ACTIVE, startOfTargetDay, endOfTargetDay);

        if (expiringSubs.isEmpty()) {
            log.info("✅ Không có gói cước nào hết hạn sau 3 ngày nữa.");
            return;
        }

        // 3. Gửi email cho từng công ty
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (CompanySubscription sub : expiringSubs) {
            Company company = sub.getCompany();
            
            // Nếu khách đã chủ động bấm Hủy (cancelAtPeriodEnd = true) thì KHÔNG làm phiền họ nữa
            if (Boolean.TRUE.equals(sub.getCancelAtPeriodEnd())) {
                continue; 
            }

            String expireDateStr = sub.getCurrentPeriodEnd().format(formatter);
            String planName = sub.getPlan().getName();
            
            // Xây dựng nội dung Email (HTML)
            String subject = "⚠️ [Cảnh báo] Gói cước " + planName + " của bạn sắp hết hạn!";
            String emailBody = String.format(
                "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;\">" +
                "  <div style=\"text-align: center; padding-bottom: 20px;\">" +
                "    <h2 style=\"color: #e67e22; margin: 0;\">THÔNG BÁO SẮP HẾT HẠN GÓI CƯỚC</h2>" +
                "  </div>" +
                "  <p>Xin chào <b>%s</b>,</p>" +
                "  <p>Hệ thống Worknet xin thông báo: Gói dịch vụ <b>%s</b> của bạn sẽ chính thức hết hạn vào lúc <b>%s</b> (Tức là khoảng 3 ngày nữa).</p>" +
                "  <div style=\"background-color: #fdf2e9; padding: 15px; border-left: 4px solid #e67e22; margin: 20px 0;\">" +
                "    <p style=\"margin: 0; color: #d35400;\"><b>Lưu ý quan trọng:</b> Nếu không được gia hạn kịp thời, tài khoản của bạn sẽ bị tạm chuyển về gói Miễn phí (FREE). Một số tài nguyên (Dự án, Thành viên) vượt quá giới hạn của gói FREE có thể sẽ bị khóa tạm thời.</p>" +
                "  </div>" +
                "  <p>Để đảm bảo công việc của đội ngũ không bị gián đoạn, vui lòng truy cập vào hệ thống và thực hiện gia hạn gói cước sớm nhất có thể.</p>" +
                "  <div style=\"text-align: center; margin-top: 30px;\">" +
                "    <a href=\"%s/admin/company/billing\" style=\"background-color: #e67e22; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;\">Gia hạn ngay</a>" +
                "  </div>" +
                "  <hr style=\"border: none; border-top: 1px solid #e0e0e0; margin-top: 30px;\">" +
                "  <p style=\"font-size: 12px; color: #7f8c8d; text-align: center;\">Cảm ơn bạn đã luôn tin tưởng và sử dụng dịch vụ của chúng tôi.</p>" +
                "</div>",
                company.getName(), planName, expireDateStr, "http://localhost:3000" // Đổi URL theo Frontend của bạn
            );

            // Gửi mail (Hàm đã có @Async nên sẽ không làm block vòng lặp)
            emailService.sendEmail(company.getEmail(), subject, emailBody);
            
            log.info("📧 Đã gửi email cảnh báo hết hạn cho công ty [{}] - Email: {}", company.getName(), company.getEmail());
        }

        log.info("✅ Hoàn tất gửi {} email cảnh báo.", expiringSubs.size());
    }
}