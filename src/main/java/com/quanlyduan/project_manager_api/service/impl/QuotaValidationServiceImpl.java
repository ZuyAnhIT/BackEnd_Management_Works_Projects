package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.exception.OverageException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.service.QuotaValidationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuotaValidationServiceImpl implements QuotaValidationService {

    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;

    // ========================================================================
    // HÀM HELPER: Lấy gói cước ĐANG HOẠT ĐỘNG của công ty
    // ========================================================================
    private CompanySubscription getActiveSubscription(Company company) {
        return company.getSubscriptions().stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new OverageException("Không tìm thấy thông tin gói cước đang hoạt động. Vui lòng liên hệ Admin."));
    }

    /**
     * Kiểm tra xem Công ty có được phép tạo thêm Dự án không.
     * Nếu vượt quá hoặc bằng giới hạn -> Ném lỗi OverageException (402).
     */
    @Override
    @Transactional(readOnly = true)
    public void validateProjectCreationQuota(Integer companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        // Dùng hàm helper để lấy gói ACTIVE
        CompanySubscription sub = getActiveSubscription(company);

        if (sub.getPlan() == null) {
            throw new OverageException("Gói cước bị lỗi dữ liệu Plan. Vui lòng liên hệ Admin.");
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

    @Override
    @Transactional(readOnly = true)
    public void validateWorkspaceCreationQuota(Integer companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        // Dùng hàm helper để lấy gói ACTIVE
        CompanySubscription sub = getActiveSubscription(company);

        if (sub.getPlan() == null) {
            throw new OverageException("Gói cước bị lỗi dữ liệu Plan. Vui lòng liên hệ Admin.");
        }

        Integer maxWorkspaces = sub.getPlan().getMaxWorkspaces();

        // Nếu maxWorkspaces = -1 (Gói Enterprise / Không giới hạn) -> Cho qua luôn
        if (maxWorkspaces == null || maxWorkspaces == -1) {
            return;
        }

        // Đếm số Workspace đang tồn tại (bỏ qua những cái đã DELETED)
        long currentWorkspaceCount = workspaceRepository.countByCompany_IdAndStatusNot(companyId, WorkspaceStatus.DELETED);

        // Chặn đứng nếu chạm hoặc vượt ngưỡng
        if (currentWorkspaceCount >= maxWorkspaces) {
            throw new OverageException(
                "Bạn đã đạt giới hạn tối đa " + maxWorkspaces + " không gian làm việc (workspace) của gói " + sub.getPlan().getName() + 
                ". Vui lòng nâng cấp gói cước hoặc xóa bớt không gian làm việc cũ để tạo mới."
            );
        }
    }
    
    // Tương tự, bạn có thể viết thêm các hàm:
    // public void validateUserInvitationQuota(Integer companyId) { ... }
    // public void validateStorageQuota(Integer companyId, long fileSizeToUpload) { ... }
}