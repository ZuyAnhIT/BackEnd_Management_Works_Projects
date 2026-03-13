package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.exception.OverageException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
// Bạn có thể inject thêm CompanyMemberRepository để làm hàm check User Quota sau này
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuotaValidationServiceImpl { // Nếu có Interface thì bạn implements vào nhé

    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;

    /**
     * Kiểm tra xem Công ty có được phép tạo thêm Dự án không.
     * Nếu vượt quá hoặc bằng giới hạn -> Ném lỗi 402 Payment Required.
     */
    @Transactional(readOnly = true)
    public void validateProjectCreationQuota(Integer companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        CompanySubscription sub = company.getSubscription();
        
        // Nếu không có gói cước (trường hợp lỗi data), mặc định không cho tạo để an toàn
        if (sub == null || sub.getPlan() == null) {
            throw new OverageException("Không tìm thấy thông tin gói cước. Vui lòng liên hệ Admin.");
        }

        int maxProjects = sub.getPlan().getMaxProjects();

        // Nếu maxProjects = -1 (Gói không giới hạn) -> Cho phép đi tiếp luôn
        if (maxProjects == -1) {
            return;
        }

        // Đếm số dự án thực tế đang hoạt động
        long currentProjects = projectRepository.countByWorkspace_Company_IdAndStatusNot(companyId, ProjectStatus.CANCELLED);

        // Nếu Số lượng hiện tại >= Số lượng cho phép -> Chặn đứng!
        if (currentProjects >= maxProjects) {
            throw new OverageException(
                "Bạn đã đạt giới hạn tối đa " + maxProjects + " dự án của gói " + sub.getPlan().getName() + 
                ". Vui lòng nâng cấp gói cước để tạo thêm dự án mới."
            );
        }
    }
    
    // Tương tự, bạn có thể viết thêm các hàm:
    // public void validateUserInvitationQuota(Integer companyId) { ... }
    // public void validateStorageQuota(Integer companyId, long fileSizeToUpload) { ... }
}