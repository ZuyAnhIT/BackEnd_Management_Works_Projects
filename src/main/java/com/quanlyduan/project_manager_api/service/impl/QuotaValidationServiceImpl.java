package com.quanlyduan.project_manager_api.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.exception.OverageException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.repository.CompanyInvitationRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.service.QuotaValidationService;

@Service
public class QuotaValidationServiceImpl implements QuotaValidationService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ERROR_COMPANY_NOT_FOUND = "Company not found.";
    public static final String ERROR_ACTIVE_SUB_NOT_FOUND = "Active subscription not found. Please contact Admin.";
    public static final String ERROR_PLAN_DATA_INVALID = "Subscription plan data is invalid. Please contact Admin.";
    public static final String ERROR_PROJECT_QUOTA_EXCEEDED = "You have reached the maximum limit of %d projects for the %s plan. Please upgrade your subscription to create more projects.";
    public static final String ERROR_WORKSPACE_QUOTA_EXCEEDED = "You have reached the maximum limit of %d workspaces for the %s plan. Please upgrade your subscription or delete old workspaces to create new ones.";
    public static final String ERROR_USER_QUOTA_EXCEEDED = "Cannot send invitation! Your '%s' plan allows a maximum of %d members. You currently have %d active members and %d pending invitations. Please cancel some pending invitations or upgrade your plan.";
    public static final String ERROR_STORAGE_QUOTA_EXCEEDED = "Insufficient storage. The %s plan allows a maximum of %d GB. Please upgrade your subscription to upload more files.";

    public static final int UNLIMITED_QUOTA = -1;
    public static final long BYTES_PER_GB = 1073741824L;

    // Khai bao cac bien phu thuoc
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final CompanyInvitationRepository companyInvitationRepository;

    // Constructor khoi tao thu cong thay the cho @RequiredArgsConstructor
    public QuotaValidationServiceImpl(CompanyRepository companyRepository,
                                      ProjectRepository projectRepository,
                                      WorkspaceRepository workspaceRepository,
                                      CompanyMemberRepository companyMemberRepository,
                                      CompanyInvitationRepository companyInvitationRepository) {
        this.companyRepository = companyRepository;
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.companyInvitationRepository = companyInvitationRepository;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional(readOnly = true)
    public void validateProjectCreationQuota(Integer companyId) {
        // Kiem tra thong tin cong ty
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        // Lay thong tin goi cuoc dang hoat dong
        CompanySubscription sub = getActiveSubscription(company);

        if (sub.getPlan() == null) {
            throw new OverageException(ERROR_PLAN_DATA_INVALID);
        }

        int maxProjects = sub.getPlan().getMaxProjects();

        // Neu goi cuoc khong gioi han du an thi cho phep tiep tuc
        if (maxProjects == UNLIMITED_QUOTA) {
            return;
        }

        // Dem so luong du an hien tai dang hoat dong
        long currentProjects = projectRepository.countByWorkspace_Company_IdAndStatusNot(companyId, ProjectStatus.CANCELLED);

        // Chan lai neu so luong du an vuot muc cho phep
        if (currentProjects >= maxProjects) {
            throw new OverageException(String.format(ERROR_PROJECT_QUOTA_EXCEEDED, maxProjects, sub.getPlan().getName()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateWorkspaceCreationQuota(Integer companyId) {
        // Kiem tra thong tin cong ty
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        // Lay thong tin goi cuoc dang hoat dong
        CompanySubscription sub = getActiveSubscription(company);

        if (sub.getPlan() == null) {
            throw new OverageException(ERROR_PLAN_DATA_INVALID);
        }

        Integer maxWorkspaces = sub.getPlan().getMaxWorkspaces();

        // Neu goi cuoc khong gioi han khong gian lam viec thi cho phep tiep tuc
        if (maxWorkspaces == null || maxWorkspaces == UNLIMITED_QUOTA) {
            return;
        }

        // Dem so luong khong gian lam viec hien tai, bo qua nhung khong gian da xoa
        long currentWorkspaceCount = workspaceRepository.countByCompany_IdAndStatusNot(companyId, WorkspaceStatus.DELETED);

        // Chan lai neu so luong khong gian lam viec vuot muc cho phep
        if (currentWorkspaceCount >= maxWorkspaces) {
            throw new OverageException(String.format(ERROR_WORKSPACE_QUOTA_EXCEEDED, maxWorkspaces, sub.getPlan().getName()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateUserInvitationQuota(Integer companyId) {
        // Kiem tra thong tin cong ty
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        // Lay thong tin goi cuoc dang hoat dong
        CompanySubscription sub = getActiveSubscription(company);
        int maxUsers = sub.getPlan().getMaxUsers();
        
        // Neu goi cuoc khong gioi han so luong nguoi dung thi cho phep tiep tuc
        if (maxUsers == UNLIMITED_QUOTA) {
            return;
        }

        // Dem so luong thanh vien chinh thuc dang hoat dong
        long currentActiveMembers = companyMemberRepository.countByCompany_IdAndStatusNot(companyId, MemberStatus.REMOVED);

        // Dem so luong loi moi dang cho xac nhan
        long currentPendingInvitations = companyInvitationRepository.countByCompany_IdAndStatus(companyId, InvitationStatus.PENDING);

        // Tinh tong so luong chiem dung
        long totalReservedSlots = currentActiveMembers + currentPendingInvitations;

        // Chan lai neu tong so luong vuot muc cho phep
        if (totalReservedSlots >= maxUsers) {
            throw new OverageException(String.format(ERROR_USER_QUOTA_EXCEEDED, sub.getPlan().getName(), maxUsers, currentActiveMembers, currentPendingInvitations));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateStorageQuota(Integer companyId, long fileSizeToUpload) {
        // Kiem tra thong tin cong ty
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        // Lay thong tin goi cuoc dang hoat dong
        CompanySubscription sub = getActiveSubscription(company);

        if (sub.getPlan() == null) {
            throw new OverageException(ERROR_PLAN_DATA_INVALID);
        }

        int maxStorageGb = sub.getPlan().getMaxStorageGb();
        
        // Neu goi cuoc khong gioi han dung luong luu tru thi cho phep tiep tuc
        if (maxStorageGb == UNLIMITED_QUOTA) {
            return;
        }

        // Quy doi tu GB sang Bytes de so sanh
        long maxStorageBytes = maxStorageGb * BYTES_PER_GB;
        long currentStorageBytes = company.getCurrentStorageBytes() != null ? company.getCurrentStorageBytes() : 0;

        // Chan lai neu tong dung luong vuot muc cho phep sau khi cong them file moi
        if (currentStorageBytes + fileSizeToUpload > maxStorageBytes) {
            throw new OverageException(String.format(ERROR_STORAGE_QUOTA_EXCEEDED, sub.getPlan().getName(), maxStorageGb));
        }
    }

    // --- CAC HAM PRIVATE HO TRO NGHIEP VU ---

    private CompanySubscription getActiveSubscription(Company company) {
        // Loc va tra ve goi cuoc dang trong trang thai hoat dong
        return company.getSubscriptions().stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new OverageException(ERROR_ACTIVE_SUB_NOT_FOUND));
    }
}