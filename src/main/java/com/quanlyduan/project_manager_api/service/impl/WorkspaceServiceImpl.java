package com.quanlyduan.project_manager_api.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.aop.ActivityLogContext;
import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.WorkspaceMember;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.CompanySubscriptionRepository;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.repository.specification.WorkspaceMemberSpecification;
import com.quanlyduan.project_manager_api.repository.specification.WorkspaceSpecification;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.FileStorageService;
import com.quanlyduan.project_manager_api.service.WorkspaceService;
import com.quanlyduan.project_manager_api.util.SortUtils;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    // Khai bao cac hang so thong bao loi
    public static final String ERROR_COMPANY_NOT_FOUND = "Company not found.";
    public static final String ERROR_WORKSPACE_NOT_FOUND = "Workspace not found.";
    public static final String ERROR_USER_NOT_FOUND = "User not found.";
    public static final String ERROR_ROLE_NOT_FOUND = "Role not found.";
    public static final String ERROR_WORKSPACE_NAME_EXISTS = "Workspace name already exists in this company.";
    public static final String ERROR_WORKSPACE_ALREADY_DELETED = "Workspace is already marked as deleted.";
    public static final String ERROR_COMPANY_MISMATCH = "Mismatched company ID.";
    public static final String ERROR_WORKSPACE_MISMATCH = "Mismatched workspace ID.";
    public static final String ERROR_STATUS_ALREADY_SET = "Status is already the requested value.";
    public static final String ERROR_STATUS_UNCHANGED = "Status unchanged.";
    public static final String ERROR_ROLE_UNCHANGED = "Role unchanged.";
    public static final String ERROR_MEMBER_NOT_FOUND = "Member not found.";
    public static final String ERROR_NOT_ACTIVE_COMPANY_MEMBER = "This user is not an active member of the company.";
    public static final String ERROR_ALREADY_WORKSPACE_MEMBER = "Already a member of this workspace.";
    public static final String ERROR_CHANGE_OWN_STATUS = "Cannot change your own status.";
    public static final String ERROR_CHANGE_OWN_ROLE = "Cannot change your own role.";
    public static final String ERROR_REMOVE_OWN_ACCOUNT = "You cannot remove yourself from the workspace.";
    public static final String ERROR_ALREADY_REMOVED = "This member has already been removed from the workspace.";
    public static final String ERROR_INVALID_ROLE_LEVEL = "Invalid role level.";
    public static final String ERROR_USE_DELETE_API = "Use the delete API to remove a member.";

    // Khai bao cac hang so hanh dong log
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_INVITE = "INVITE";
    public static final String ACTION_REMOVE = "REMOVE";
    public static final String ENTITY_WORKSPACE = "WORKSPACE";
    public static final String ENTITY_WORKSPACE_MEMBER = "WORKSPACE_MEMBER";

    // Khai bao cac hang so ghi log chi tiet
    public static final String LOG_DESC_CREATE = "Create new Workspace";
    public static final String LOG_DESC_UPDATE = "Update Workspace";
    public static final String LOG_DESC_DELETE = "Delete Workspace";
    public static final String LOG_DESC_INVITE = "Invite member to Workspace";
    public static final String LOG_DESC_REMOVE = "Remove member from Workspace";
    public static final String LOG_RENAMED = "renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"";
    public static final String LOG_DESC_UPDATED = "updated description";
    public static final String LOG_COVER_UPDATED = "updated cover image";

    // Khai bao cac hang so cau hinh
    public static final String DEFAULT_COLOR = "#3498db";
    public static final String FOLDER_WORKSPACE_COVERS = "workspace-covers";
    public static final String FILE_API_PATH = "/api/files";
    public static final String HTTP_PREFIX = "http";
    public static final String GUEST_ROLE_CODE = "GUEST";

    // Khai bao email template
    public static final String EMAIL_SUBJECT_ADDED = "Added to Workspace: ";
    public static final String EMAIL_BODY_TEMPLATE = 
        "<p>Hello %s,</p>" +
        "<p>You have been added to the workspace <strong>%s</strong> by %s.</p>" +
        "<ul>" +
        "<li><strong>Your Role:</strong> %s</li>" +
        "<li><strong>Company:</strong> %s</li>" +
        "</ul>" +
        "<p>You can access the workspace immediately by clicking on <a href=\"%s\">this link</a>.</p>" +
        "<p>Thank you,<br>Project Management Team</p>";

    // Khai bao cac bien phu thuoc
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;
    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final QuotaValidationServiceImpl quotaValidationService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // Constructor thiet lap thu cong cac dependency
    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository,
                                WorkspaceMemberRepository workspaceMemberRepository,
                                CompanyRepository companyRepository,
                                RoleRepository roleRepository,
                                SecurityService securityService,
                                UserRepository userRepository,
                                CompanyMemberRepository companyMemberRepository,
                                EmailService emailService,
                                FileStorageService fileStorageService,
                                CompanySubscriptionRepository companySubscriptionRepository,
                                QuotaValidationServiceImpl quotaValidationService) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.securityService = securityService;
        this.userRepository = userRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.emailService = emailService;
        this.fileStorageService = fileStorageService;
        this.companySubscriptionRepository = companySubscriptionRepository;
        this.quotaValidationService = quotaValidationService;
    }

    // --- CAC HAM PUBLIC QUAN LY WORKSPACE ---

    @Override
    @Transactional
    @LogActivity(action = ACTION_CREATE, entityType = ENTITY_WORKSPACE, description = LOG_DESC_CREATE)
    public WorkspaceResponse createWorkspace(Integer companyId, CreateWorkspaceRequest request, MultipartFile coverImageFile) {
        // Kiem tra han muc so luong khong gian lam viec cho phep
        quotaValidationService.validateWorkspaceCreationQuota(companyId);

        // Lay thong tin nguoi thuc hien va kiem tra ton tai cong ty
        User creator = securityService.getCurrentAuthenticatedUser();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        // Kiem tra ten khong gian lam viec da ton tai trong cong ty hay chua
        if (workspaceRepository.existsByCompany_IdAndName(companyId, request.getWorkspaceName())) {
            throw new BadRequestException(ERROR_WORKSPACE_NAME_EXISTS);
        }

        // Tim kiem vai tro quan tri cho khong gian lam viec
        Role workspaceAdminRole = roleRepository.findFirstByRoleCode(RoleCode.WORKSPACE_ADMIN.name())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ROLE_NOT_FOUND));

        // Khoi tao thuc the khong gian lam viec
        Workspace newWorkspace = Workspace.builder()
                .company(company)
                .name(request.getWorkspaceName())
                .description(request.getDescription())
                .color(request.getColor() != null ? request.getColor() : DEFAULT_COLOR)
                .createdBy(creator)
                .status(WorkspaceStatus.ACTIVE)
                .build();

        // Xu ly tai len anh bia neu co
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String coverPath = fileStorageService.storeFile(coverImageFile, FOLDER_WORKSPACE_COVERS);
            newWorkspace.setCoverImageUrl(coverPath);
        } else if (request.getCoverImage() != null) {
            newWorkspace.setCoverImageUrl(request.getCoverImage());
        }

        Workspace savedWorkspace = workspaceRepository.save(newWorkspace);

        // Gan nguoi tao thanh quan tri vien cua khong gian lam viec moi
        WorkspaceMember membership = WorkspaceMember.builder()
                .workspace(savedWorkspace)
                .user(creator)
                .role(workspaceAdminRole)
                .status(MemberStatus.ACTIVE)
                .build();

        workspaceMemberRepository.save(membership);

        return mapToWorkspaceResponse(savedWorkspace);
    }

    @Override
    public WorkspaceResponse getWorkspaceDetails(Integer workspaceId) {
        // Lay chi tiet khong gian lam viec dua tren ID
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_WORKSPACE_NOT_FOUND));
        return mapToWorkspaceResponse(workspace);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_WORKSPACE, description = LOG_DESC_UPDATE)
    public WorkspaceResponse updateWorkspace(Integer workspaceId, UpdateWorkspaceRequest request, MultipartFile coverImageFile) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_WORKSPACE_NOT_FOUND));

        StringBuilder changes = new StringBuilder();

        // Cap nhat ten va kiem tra tinh duy nhat
        if (request.getName() != null && !request.getName().equals(workspace.getName())) {
             if (workspaceRepository.existsByCompany_IdAndName(workspace.getCompany().getId(), request.getName())) {
                  throw new BadRequestException(ERROR_WORKSPACE_NAME_EXISTS);
             }
             if (changes.length() > 0) changes.append(", ");
             changes.append(String.format(LOG_RENAMED, workspace.getName(), request.getName()));
             workspace.setName(request.getName());
        }

        // Cap nhat mo ta
        if (request.getDescription() != null && !request.getDescription().equals(workspace.getDescription())) {
             if (changes.length() > 0) changes.append(", ");
             changes.append(LOG_DESC_UPDATED);
             workspace.setDescription(request.getDescription());
        }
        
        // Cap nhat mau sac
        if (request.getColor() != null && !request.getColor().equals(workspace.getColor())) {
             workspace.setColor(request.getColor());
        }

        // Cap nhat anh bia thong qua file tai len hoac duong dan truc tiep
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String path = fileStorageService.storeFile(coverImageFile, FOLDER_WORKSPACE_COVERS);
            if (changes.length() > 0) changes.append(", ");
            changes.append(LOG_COVER_UPDATED);
            workspace.setCoverImageUrl(path);
        } else if (request.getCoverImage() != null && !request.getCoverImage().equals(workspace.getCoverImageUrl())) {
            workspace.setCoverImageUrl(request.getCoverImage());
        }

        // Ghi nhan lich su cac thay doi vao context hoat dong
        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        }

        return mapToWorkspaceResponse(workspaceRepository.save(workspace));
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_DELETE, entityType = ENTITY_WORKSPACE, description = LOG_DESC_DELETE)
    public void deleteWorkspace(Integer workspaceId) {
        // Tim kiem va xac thuc trang thai xoa cua khong gian lam viec
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_WORKSPACE_NOT_FOUND));
        
        if (workspace.getStatus() == WorkspaceStatus.DELETED) {
            throw new BadRequestException(ERROR_WORKSPACE_ALREADY_DELETED);
        }

        workspace.setStatus(WorkspaceStatus.DELETED);
        workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspaceStatus(Integer companyId, Integer workspaceId, UpdateWorkspaceStatusRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_WORKSPACE_NOT_FOUND));

        if (!workspace.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(ERROR_COMPANY_MISMATCH);
        }

        if (workspace.getStatus() == request.getNewStatus()) {
            throw new BadRequestException(ERROR_STATUS_ALREADY_SET);
        }

        workspace.setStatus(request.getNewStatus());
        return mapToWorkspaceResponse(workspaceRepository.save(workspace));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<WorkspaceResponse> getWorkspacesByCompany(Integer companyId, int page, int size, String sortBy, String sortDir) {
        // Anh xa cac truong sap xep duoc phep cho khong gian lam viec
        Map<String, String> workspaceMapping = Map.of(
            "createdAt", "createdAt",
            "name", "name",
            "code", "workspaceCode",
            "status", "status",
            "createdBy", "createdBy.fullName"
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDir, "createdAt", workspaceMapping);

        // Truy van danh sach phan trang tu repository
        Page<Workspace> workspacePage = workspaceRepository.findByCompany_Id(companyId, pageable);
        Page<WorkspaceResponse> dtoPage = workspacePage.map(this::mapToWorkspaceResponse);
        
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<WorkspaceResponse> searchWorkspaces(
            Integer companyId,
            String searchName, String searchCode, String searchDescription, WorkspaceStatus searchStatus,
            int page, int size, String sortBy, String sortDir) {

        Map<String, String> workspaceMapping = Map.of(
            "createdAt", "createdAt",
            "name", "name",
            "code", "workspaceCode",
            "status", "status",
            "createdBy", "createdBy.fullName"
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDir, "createdAt", workspaceMapping);

        // Ap dung bo loc tim kiem dong dua tren Specification
        Specification<Workspace> spec = WorkspaceSpecification.filterWorkspaces(
            companyId, searchName, searchCode, searchDescription, searchStatus
        );

        Page<Workspace> workspacePage = workspaceRepository.findAll(spec, pageable);
        Page<WorkspaceResponse> dtoPage = workspacePage.map(this::mapToWorkspaceResponse);
        
        return new PageResponseDTO<>(dtoPage);
    }

    // --- CAC HAM PUBLIC QUAN LY THANH VIEN WORKSPACE ---

    @Override
    public WorkspaceMemberResponse getWorkspaceMemberDetails(Integer workspaceId, Integer memberId) {
        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_MEMBER_NOT_FOUND));

        if (!member.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException(ERROR_WORKSPACE_MISMATCH);
        }

        return mapToWorkspaceMemberResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<WorkspaceMemberResponse> getWorkspaceMembers(
            Integer workspaceId, int page, int size, String sortBy, String sortDir) {

        // Anh xa cac truong sap xep cho thanh vien khong gian lam viec
        Map<String, String> memberMapping = Map.of(
            "joinedAt", "joinedAt",
            "createdAt", "joinedAt",
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "phone", "user.phoneNumber"
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", memberMapping);

        Page<WorkspaceMember> membersPage = workspaceMemberRepository.findByWorkspace_Id(workspaceId, pageable);
        Page<WorkspaceMemberResponse> dtoPage = membersPage.map(this::mapToWorkspaceMemberResponse);
        
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<WorkspaceMemberResponse> searchWorkspaceMembers(
            Integer workspaceId,
            String searchName, String searchEmail, String searchRoleName, String searchPhone,
            int page, int size, String sortBy, String sortDir) {

        Map<String, String> memberMapping = Map.of(
            "joinedAt", "joinedAt",
            "createdAt", "joinedAt",
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "phone", "user.phoneNumber"
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", memberMapping);

        Specification<WorkspaceMember> spec = WorkspaceMemberSpecification.filterMembers(
            workspaceId, searchName, searchEmail, searchRoleName, searchPhone
        );

        Page<WorkspaceMember> membersPage = workspaceMemberRepository.findAll(spec, pageable);
        Page<WorkspaceMemberResponse> dtoPage = membersPage.map(this::mapToWorkspaceMemberResponse);
        
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_INVITE, entityType = ENTITY_WORKSPACE_MEMBER, description = LOG_DESC_INVITE) 
    public WorkspaceMember inviteMemberToWorkspace(Integer companyId, Integer workspaceId, InviteWorkspaceMemberRequest request) {
        User admin = securityService.getCurrentAuthenticatedUser();
        User userToInvite = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND));

        // Xac thuc thanh vien phai thuoc cong ty thi moi duoc moi vao khong gian lam viec
        if (!companyMemberRepository.existsByCompany_IdAndUser_IdAndStatus(companyId, userToInvite.getId(), MemberStatus.ACTIVE)) {
            throw new BadRequestException(ERROR_NOT_ACTIVE_COMPANY_MEMBER);
        }

        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(
                () -> new ResourceNotFoundException(ERROR_WORKSPACE_NOT_FOUND)
        );

        Role role = roleRepository.findFirstByRoleCode(request.getRoleCode()).orElseThrow(
                () -> new ResourceNotFoundException(ERROR_ROLE_NOT_FOUND)
        );

        // Kiem tra neu nguoi dung da la thanh vien cua khong gian lam viec nay
        if (workspaceMemberRepository.findByWorkspace_IdAndUser_Id(workspaceId, userToInvite.getId()).isPresent()) {
            throw new BadRequestException(ERROR_ALREADY_WORKSPACE_MEMBER);
        }

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(userToInvite)
                .role(role)
                .status(MemberStatus.ACTIVE)
                .build();
        
        WorkspaceMember savedMember = workspaceMemberRepository.save(member);
        
        // Gui email thong bao cho thanh vien moi gia nhap
        sendWorkspaceNotificationEmail(admin, userToInvite, workspace, role);

        return savedMember;
    }

    @Override
    @Transactional
    public WorkspaceMemberResponse updateWorkspaceMemberStatus(Integer companyId, Integer workspaceId, Integer memberId, UpdateMemberStatusRequest request) {
        WorkspaceMember member = workspaceMemberRepository.findById(memberId).orElseThrow(
                () -> new ResourceNotFoundException(ERROR_MEMBER_NOT_FOUND)
        );

        if (!member.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException(ERROR_WORKSPACE_MISMATCH);
        }
        if (!member.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(ERROR_COMPANY_MISMATCH);
        }

        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) {
            throw new BadRequestException(ERROR_CHANGE_OWN_STATUS);
        }

        if (request.getNewStatus() == MemberStatus.REMOVED) {
            throw new BadRequestException(ERROR_USE_DELETE_API);
        }
        
        if (member.getStatus() == request.getNewStatus()) {
            throw new BadRequestException(ERROR_STATUS_UNCHANGED);
        }

        member.setStatus(request.getNewStatus());
        return mapToWorkspaceMemberResponse(workspaceMemberRepository.save(member));
    }

    @Override
    @Transactional
    public WorkspaceMemberResponse updateWorkspaceMemberRole(Integer companyId, Integer workspaceId, Integer memberId, String newRoleCode) {
        WorkspaceMember member = workspaceMemberRepository.findById(memberId).orElseThrow(
                () -> new ResourceNotFoundException(ERROR_MEMBER_NOT_FOUND)
        );

        if (!member.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException(ERROR_WORKSPACE_MISMATCH);
        }
        if (!member.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(ERROR_COMPANY_MISMATCH);
        }

        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) {
            throw new BadRequestException(ERROR_CHANGE_OWN_ROLE);
        }

        Role role = roleRepository.findFirstByRoleCode(newRoleCode).orElseThrow(
                () -> new ResourceNotFoundException(ERROR_ROLE_NOT_FOUND)
        );
        
        if (role.getLevel() != RoleLevel.WORKSPACE) {
            throw new BadRequestException(ERROR_INVALID_ROLE_LEVEL);
        }

        if (member.getRole().getRoleCode().equals(newRoleCode)) {
            throw new BadRequestException(ERROR_ROLE_UNCHANGED);
        }
        
        if (member.getStatus() == MemberStatus.REMOVED) {
            throw new BadRequestException(ERROR_ALREADY_REMOVED);
        }

        member.setRole(role);
        return mapToWorkspaceMemberResponse(workspaceMemberRepository.save(member));
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_REMOVE, entityType = ENTITY_WORKSPACE_MEMBER, description = LOG_DESC_REMOVE) 
    public void removeMemberFromWorkspace(Integer companyId, Integer workspaceId, Integer memberId) {
        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_MEMBER_NOT_FOUND));

        if (!member.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException(ERROR_WORKSPACE_MISMATCH);
        }
        if (!member.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(ERROR_COMPANY_MISMATCH);
        }

        User currentUser = securityService.getCurrentAuthenticatedUser();
        if (currentUser.getId().equals(member.getUser().getId())) {
            throw new BadRequestException(ERROR_REMOVE_OWN_ACCOUNT);
        }

        if (member.getStatus() == MemberStatus.REMOVED) {
            throw new BadRequestException(ERROR_ALREADY_REMOVED);
        }

        // Xoa mem thanh vien bang cach chuyen trang thai sang REMOVED
        member.setStatus(MemberStatus.REMOVED);
        workspaceMemberRepository.save(member);
    }

    // --- CAC HAM PRIVATE HO TRO (HELPERS) ---

    private Pageable createPageable(int page, int size, String sortBy, String sortDir,
                                     String defaultSortField, Map<String, String> sortMapping) {
        Sort sort = SortUtils.createSort(sortBy, sortDir, defaultSortField, sortMapping);
        return PageRequest.of(page, size, sort);
    }

    private void sendWorkspaceNotificationEmail(User admin, User userAdded, Workspace workspace, Role role) {
        try {
            String workspaceUrl = String.format("%s/companies/%d/workspaces/%d", 
                frontendUrl, workspace.getCompany().getId(), workspace.getId());
            
            String emailBody = String.format(EMAIL_BODY_TEMPLATE,
                userAdded.getFullName(), 
                workspace.getName(), 
                admin.getFullName(), 
                role.getRoleName(), 
                workspace.getCompany().getName(), 
                workspaceUrl
            );

            emailService.sendEmail(userAdded.getEmail(), EMAIL_SUBJECT_ADDED + workspace.getName(), emailBody);
        } catch (Exception e) {
            // Su dung System.err de ghi log loi gui email cho production ma khong anh huong luong xu ly chinh
            System.err.println("Email Delivery Error: " + e.getMessage());
        }
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private WorkspaceResponse mapToWorkspaceResponse(Workspace kg) {
        String coverUrl = kg.getCoverImageUrl();
        if (coverUrl != null && !coverUrl.isBlank() && !coverUrl.startsWith(HTTP_PREFIX)) {
            coverUrl = FILE_API_PATH + coverUrl; 
        }

        return WorkspaceResponse.builder()
                .workspaceId(kg.getId())
                .companyId(kg.getCompany().getId())
                .workspaceName(kg.getName())
                .description(kg.getDescription())
                .coverImage(coverUrl)
                .color(kg.getColor())
                .createdById(kg.getCreatedBy().getId())
                .status(kg.getStatus().name())
                .createdAt(kg.getCreatedAt())
                .build();
    }

    private WorkspaceMemberResponse mapToWorkspaceMemberResponse(WorkspaceMember member) {
        return WorkspaceMemberResponse.builder()
                .memberId(member.getId())
                .userId(member.getUser().getId())
                .fullName(member.getUser().getFullName())
                .email(member.getUser().getEmail())
                .avatarUrl(member.getUser().getAvatarUrl())
                .phoneNumber(member.getUser().getPhoneNumber())
                .roleName(member.getRole().getRoleName())
                .joinedAt(member.getJoinedAt())
                .status(member.getStatus())
                .build();
    }
}