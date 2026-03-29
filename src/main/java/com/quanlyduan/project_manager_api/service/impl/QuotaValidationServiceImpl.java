package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.exception.OverageException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus; // <-- Đã thêm
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.repository.CompanyInvitationRepository; // <-- Đã thêm
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
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
    private final CompanyMemberRepository companyMemberRepository; 
    private final CompanyInvitationRepository companyInvitationRepository; // <-- Đã khai báo biến

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
    
    /**
     * KIỂM TRA HẠN MỨC THÀNH VIÊN (USER QUOTA)
     */
    @Override
    @Transactional(readOnly = true)
    public void validateUserInvitationQuota(Integer companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        CompanySubscription sub = getActiveSubscription(company);
        int maxUsers = sub.getPlan().getMaxUsers();
        if (maxUsers == -1) return; // Không giới hạn

        // 1. Đếm thành viên chính thức
        long currentActiveMembers = companyMemberRepository.countByCompany_IdAndStatusNot(companyId, MemberStatus.REMOVED);
        
        // 2. Đếm lời mời đang treo
        long currentPendingInvitations = companyInvitationRepository.countByCompany_IdAndStatus(companyId, InvitationStatus.PENDING);
        
        // 3. Tính tổng Slot chiếm dụng
        long totalReservedSlots = currentActiveMembers + currentPendingInvitations;

        if (totalReservedSlots >= maxUsers) {
            throw new OverageException(
                String.format("Cannot send invitation! Your '%s' plan allows a maximum of %d members. " +
                              "You currently have %d active members and %d pending invitations. " +
                              "Please cancel some pending invitations or upgrade your plan.", 
                sub.getPlan().getName(), maxUsers, currentActiveMembers, currentPendingInvitations)
            );
        }
    }

    /**
     * KIỂM TRA HẠN MỨC DUNG LƯỢNG (STORAGE QUOTA)
     */
    @Override
    @Transactional(readOnly = true)
    public void validateStorageQuota(Integer companyId, long fileSizeToUpload) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        CompanySubscription sub = getActiveSubscription(company);

        if (sub.getPlan() == null) {
            throw new OverageException("Gói cước bị lỗi dữ liệu Plan. Vui lòng liên hệ Admin.");
        }

        int maxStorageGb = sub.getPlan().getMaxStorageGb();
        if (maxStorageGb == -1) return; // Không giới hạn

        // Quy đổi GB sang Bytes (1 GB = 1073741824 Bytes)
        long maxStorageBytes = maxStorageGb * 1073741824L;
        long currentStorageBytes = company.getCurrentStorageBytes() != null ? company.getCurrentStorageBytes() : 0;

        // Kiểm tra: Dung lượng hiện tại + Kích thước file sắp tải lên có vượt quá không?
        if (currentStorageBytes + fileSizeToUpload > maxStorageBytes) {
            throw new OverageException(
                "Không đủ dung lượng lưu trữ. Gói " + sub.getPlan().getName() + " chỉ cho phép tối đa " + 
                maxStorageGb + "GB. Vui lòng nâng cấp gói cước để tải thêm file."
            );
        }
    }
}