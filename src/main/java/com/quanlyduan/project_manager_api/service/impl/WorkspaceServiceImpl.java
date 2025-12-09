// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/WorkspaceServiceImpl.java
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

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // ========================================================================
    // CONSTRUCTOR (Dependency Injection)
    // ========================================================================
    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository,
                                 WorkspaceMemberRepository workspaceMemberRepository,
                                 CompanyRepository companyRepository,
                                 RoleRepository roleRepository,
                                 SecurityService securityService,
                                 UserRepository userRepository,
                                 CompanyMemberRepository companyMemberRepository,
                                 EmailService emailService,
                                 FileStorageService fileStorageService) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.securityService = securityService;
        this.userRepository = userRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.emailService = emailService;
        this.fileStorageService = fileStorageService;
    }

    // ========================================================================
    // NHÓM 1: QUẢN LÝ WORKSPACE (Create, Update, Details, List)
    // ========================================================================

    // LOGIC TẠO WORKSPACE (KÈM UPLOAD ẢNH BÌA)
    @Override
    @Transactional
    @LogActivity(action = "CREATE", entityType = "WORKSPACE", description = "Create new Workspace")
    public WorkspaceResponse createWorkspace(Integer companyId, CreateWorkspaceRequest request, MultipartFile coverImageFile) {

        // 1. Kiểm tra tồn tại Công ty và User tạo
        User creator = securityService.getCurrentAuthenticatedUser();
        Company company = companyRepository.findById(companyId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        // 2. Kiểm tra trùng tên Workspace trong cùng Công ty
        if (workspaceRepository.existsByCompany_IdAndName(companyId, request.getWorkspaceName())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace name already exists in this company.");
        }

        // 3. Tìm Role Admin Workspace
        Role workspaceAdminRole = roleRepository.findFirstByRoleCode(RoleCode.WORKSPACE_ADMIN.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                        // Sửa thông báo sang tiếng Anh
                        "Role not found: WORKSPACE_ADMIN. Please configure the database."
                ));

        // 4. Tạo Workspace Entity
        Workspace newWorkspace = Workspace.builder()
                .company(company)
                .name(request.getWorkspaceName())
                .description(request.getDescription())
                .color(request.getColor() != null ? request.getColor() : "#3498db")
                .createdBy(creator)
                .status(WorkspaceStatus.ACTIVE)
                .build();

        // 5. Xử lý Upload/Link Ảnh Bìa
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            // Lưu file và gán đường dẫn cục bộ
            String coverPath = fileStorageService.storeFile(coverImageFile, "workspace-covers");
            newWorkspace.setCoverImageUrl(coverPath);
        } else if (request.getCoverImage() != null) {
            // Gán đường dẫn URL từ request
            newWorkspace.setCoverImageUrl(request.getCoverImage());
        }

        Workspace savedWorkspace = workspaceRepository.save(newWorkspace);

        // 6. Gán người tạo làm thành viên Admin Workspace đầu tiên
        WorkspaceMember membership = WorkspaceMember.builder()
                .workspace(savedWorkspace)
                .user(creator)
                .role(workspaceAdminRole)
                .status(MemberStatus.ACTIVE)
                .build();

        workspaceMemberRepository.save(membership);

        // 7. Map và trả về
        return mapToWorkspaceResponse(savedWorkspace);
    }

    // LOGIC LẤY CHI TIẾT WORKSPACE
    @Override
    public WorkspaceResponse getWorkspaceDetails(Integer workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));
        return mapToWorkspaceResponse(workspace);
    }

    // LOGIC CẬP NHẬT WORKSPACE (KÈM UPLOAD ẢNH BÌA)
   @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "WORKSPACE", description = "Update Workspace")
    public WorkspaceResponse updateWorkspace(Integer workspaceId, UpdateWorkspaceRequest request, MultipartFile coverImageFile) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));

        StringBuilder changes = new StringBuilder();

        // 1. Name
        if (request.getName() != null && !request.getName().equals(workspace.getName())) {
             if (workspaceRepository.existsByCompany_IdAndName(workspace.getCompany().getId(), request.getName())) {
                  throw new BadRequestException("Workspace name already exists.");
             }
             if (changes.length() > 0) changes.append(", ");
             changes.append(String.format("renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"", workspace.getName(), request.getName()));
             workspace.setName(request.getName());
        }

        // 2. Description
        if (request.getDescription() != null && !request.getDescription().equals(workspace.getDescription())) {
             if (changes.length() > 0) changes.append(", ");
             changes.append("updated description");
             workspace.setDescription(request.getDescription());
        }
        
        // 3. Color
        if (request.getColor() != null && !request.getColor().equals(workspace.getColor())) {
             workspace.setColor(request.getColor());
        }

        // 4. Cover Image
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String path = fileStorageService.storeFile(coverImageFile, "workspace-covers");
            if (changes.length() > 0) changes.append(", ");
            changes.append("updated cover image");
            workspace.setCoverImageUrl(path);
        } else if (request.getCoverImage() != null && !request.getCoverImage().equals(workspace.getCoverImageUrl())) {
            workspace.setCoverImageUrl(request.getCoverImage());
        }

        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        } else {
            //  ActivityLogContext.setDetail("updated details");
        }

        return mapToWorkspaceResponse(workspaceRepository.save(workspace));
    }

    // LOGIC SOFT DELETE (DELETED)
    @Override
    @Transactional
    @LogActivity(action = "DELETE", entityType = "WORKSPACE", description = "Delete Workspace")
    public void deleteWorkspace(Integer workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));
        // Sửa thông báo sang tiếng Anh
        if (workspace.getStatus() == WorkspaceStatus.DELETED) throw new BadRequestException("Workspace is already marked as deleted.");
        workspace.setStatus(WorkspaceStatus.DELETED);
        workspaceRepository.save(workspace);
    }

    // LOGIC CẬP NHẬT TRẠNG THÁI WORKSPACE
    @Override
    @Transactional
    public WorkspaceResponse updateWorkspaceStatus(Integer companyId, Integer workspaceId, UpdateWorkspaceStatusRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));
        // Sửa thông báo sang tiếng Anh
        if (!workspace.getCompany().getId().equals(companyId)) throw new ResourceNotFoundException("Mismatched company ID.");
        // Sửa thông báo sang tiếng Anh
        if (workspace.getStatus() == request.getNewStatus()) throw new BadRequestException("Status is already the requested value.");

        workspace.setStatus(request.getNewStatus());
        return mapToWorkspaceResponse(workspaceRepository.save(workspace));
    }

    // LOGIC LẤY DANH SÁCH WORKSPACE (Cơ bản)
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<WorkspaceResponse> getWorkspacesByCompany(Integer companyId, int page, int size, String sortBy, String sortDir) {

        // 1. ĐỊNH NGHĨA MAP CHO WORKSPACE
        Map<String, String> workspaceMapping = Map.of(
            "createdAt", "createdAt",
            "name", "name",
            "code", "workspaceCode",
            "status", "status",
            "createdBy", "createdBy.fullName"
        );

        // 2. GỌI HELPER CHUNG
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "createdAt", workspaceMapping);

        // 3. Query DB
        Page<Workspace> workspacePage = workspaceRepository.findByCompany_Id(companyId, pageable);
        Page<WorkspaceResponse> dtoPage = workspacePage.map(this::mapToWorkspaceResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // LOGIC TÌM KIẾM WORKSPACE (Nâng cao)
    public PageResponseDTO<WorkspaceResponse> searchWorkspaces(
            Integer companyId,
            String searchName, String searchCode, String searchDescription, WorkspaceStatus searchStatus,
            int page, int size, String sortBy, String sortDir) {

        // 1. Dùng lại Map của Workspace
        Map<String, String> workspaceMapping = Map.of(
            "createdAt", "createdAt",
            "name", "name",
            "code", "workspaceCode",
            "status", "status",
            "createdBy", "createdBy.fullName"
        );

        // 2. Tạo Pageable
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "createdAt", workspaceMapping);

        // 3. Specification
        Specification<Workspace> spec = WorkspaceSpecification.filterWorkspaces(
            companyId, searchName, searchCode, searchDescription, searchStatus
        );

        // 4. Query DB
        Page<Workspace> workspacePage = workspaceRepository.findAll(spec, pageable);
        Page<WorkspaceResponse> dtoPage = workspacePage.map(this::mapToWorkspaceResponse);
        return new PageResponseDTO<>(dtoPage);
    }


    // ========================================================================
    // NHÓM 2: QUẢN LÝ THÀNH VIÊN WORKSPACE (Members)
    // ========================================================================

    // LOGIC LẤY CHI TIẾT THÀNH VIÊN
    @Override
    public WorkspaceMemberResponse getWorkspaceMemberDetails(Integer workspaceId, Integer memberId) {
        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Member not found."));
        // Sửa thông báo sang tiếng Anh
        if (!member.getWorkspace().getId().equals(workspaceId)) throw new ResourceNotFoundException("Mismatched workspace ID.");
        return mapToWorkspaceMemberResponse(member);
    }

    // LOGIC LẤY DANH SÁCH THÀNH VIÊN (Cơ bản)
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<WorkspaceMemberResponse> getWorkspaceMembers(
            Integer workspaceId, int page, int size, String sortBy, String sortDir) {

        // 1. ĐỊNH NGHĨA MAP CHO MEMBER
        Map<String, String> memberMapping = Map.of(
            "joinedAt", "joinedAt",
            "createdAt", "joinedAt",
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "phone", "user.phoneNumber"
        );

        // 2. GỌI HELPER CHUNG
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", memberMapping);

        // 3. Query DB
        Page<WorkspaceMember> membersPage = workspaceMemberRepository.findByWorkspace_Id(workspaceId, pageable);
        Page<WorkspaceMemberResponse> dtoPage = membersPage.map(this::mapToWorkspaceMemberResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // LOGIC TÌM KIẾM THÀNH VIÊN (Nâng cao)
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<WorkspaceMemberResponse> searchWorkspaceMembers(
            Integer workspaceId,
            String searchName, String searchEmail, String searchRoleName, String searchPhone,
            int page, int size, String sortBy, String sortDir) {

        // 1. Dùng lại Map của Member
        Map<String, String> memberMapping = Map.of(
            "joinedAt", "joinedAt",
            "createdAt", "joinedAt",
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "phone", "user.phoneNumber"
        );

        // 2. Tạo Pageable
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", memberMapping);

        // 3. Specification
        Specification<WorkspaceMember> spec = WorkspaceMemberSpecification.filterMembers(
            workspaceId, searchName, searchEmail, searchRoleName, searchPhone
        );

        // 4. Query DB
        Page<WorkspaceMember> membersPage = workspaceMemberRepository.findAll(spec, pageable);
        Page<WorkspaceMemberResponse> dtoPage = membersPage.map(this::mapToWorkspaceMemberResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // LOGIC MỜI THÀNH VIÊN VÀO WORKSPACE
    @Override
    @Transactional
    @LogActivity(action = "INVITE", entityType = "WORKSPACE_MEMBER", description = "Invite member to Workspace") 
    public WorkspaceMember inviteMemberToWorkspace(Integer companyId, Integer workspaceId, InviteWorkspaceMemberRequest request) {
        // 1. Lấy thông tin cần thiết
        User admin = securityService.getCurrentAuthenticatedUser();
        User userToInvite = userRepository.findByEmail(request.getEmail())
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // 2. Validate: Phải là thành viên ACTIVE của Công ty
        if (!companyMemberRepository.existsByCompany_IdAndUser_IdAndStatus(companyId, userToInvite.getId(), MemberStatus.ACTIVE)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This user is not an active member of the company.");
        }

        // 3. Lấy Workspace và Role
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(
                // Sửa thông báo sang tiếng Anh
                () -> new ResourceNotFoundException("Workspace not found.")
        );
        Role role = roleRepository.findFirstByRoleCode(request.getRoleCode()).orElseThrow(
                // Sửa thông báo sang tiếng Anh
                () -> new ResourceNotFoundException("Role not found.")
        );

        // 4. Kiểm tra: Đã là thành viên Workspace chưa?
        if (workspaceMemberRepository.findByWorkspace_IdAndUser_Id(workspaceId, userToInvite.getId()).isPresent()) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Already a member of this workspace.");
        }

        // 5. Thêm thành viên và gửi mail thông báo (Internal Member)
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(userToInvite)
                .role(role)
                .status(MemberStatus.ACTIVE)
                .build();
        WorkspaceMember savedMember = workspaceMemberRepository.save(member);
        
        // 7. Gửi mail
        sendWorkspaceNotificationEmail(admin, userToInvite, workspace, role);

        return savedMember;
    }

    // LOGIC CẬP NHẬT TRẠNG THÁI THÀNH VIÊN (ACTIVE/SUSPENDED)
    @Override
    @Transactional
    public WorkspaceMemberResponse updateWorkspaceMemberStatus(Integer companyId, Integer workspaceId, Integer memberId, UpdateMemberStatusRequest request) {
        WorkspaceMember member = workspaceMemberRepository.findById(memberId).orElseThrow(
                // Sửa thông báo sang tiếng Anh
                () -> new ResourceNotFoundException("Member not found.")
        );

        // Kiểm tra Hierarchy
        if (!member.getWorkspace().getId().equals(workspaceId)) throw new ResourceNotFoundException("Mismatched workspace ID.");
        if (!member.getWorkspace().getCompany().getId().equals(companyId)) throw new ResourceNotFoundException("Mismatched company ID.");

        // Kiểm tra nghiệp vụ: Không thể tự đổi trạng thái
        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) throw new BadRequestException("Cannot change your own status.");

        // Kiểm tra nghiệp vụ: Không dùng API này để xóa hẳn (REMOVED)
        if (request.getNewStatus() == MemberStatus.REMOVED) throw new BadRequestException("Use the delete API to remove a member.");
        // Kiểm tra nghiệp vụ: Trạng thái không thay đổi
        if (member.getStatus() == request.getNewStatus()) throw new BadRequestException("Status unchanged.");


        member.setStatus(request.getNewStatus());
        return mapToWorkspaceMemberResponse(workspaceMemberRepository.save(member));
    }

    // LOGIC CẬP NHẬT VAI TRÒ THÀNH VIÊN
    @Override
    @Transactional
    public WorkspaceMemberResponse updateWorkspaceMemberRole(Integer companyId, Integer workspaceId, Integer memberId, String newRoleCode) {
        WorkspaceMember member = workspaceMemberRepository.findById(memberId).orElseThrow(
                // Sửa thông báo sang tiếng Anh
                () -> new ResourceNotFoundException("Member not found.")
        );

        // Kiểm tra Hierarchy
        if (!member.getWorkspace().getId().equals(workspaceId)) throw new ResourceNotFoundException("Mismatched workspace ID.");
        if (!member.getWorkspace().getCompany().getId().equals(companyId)) throw new ResourceNotFoundException("Mismatched company ID.");

        // Kiểm tra nghiệp vụ: Không thể tự đổi vai trò
        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) throw new BadRequestException("Cannot change your own role.");

        // Tìm và Validate Role
        Role role = roleRepository.findFirstByRoleCode(newRoleCode).orElseThrow(
                // Sửa thông báo sang tiếng Anh
                () -> new ResourceNotFoundException("Role not found.")
        );
        if (role.getLevel() != RoleLevel.WORKSPACE) throw new BadRequestException("Invalid role level.");

        // Kiểm tra nghiệp vụ: Vai trò không thay đổi
        if (member.getRole().getRoleCode().equals(newRoleCode)) throw new BadRequestException("Role unchanged.");
        // Kiểm tra nghiệp vụ: Thành viên đã bị xóa (REMOVED)
        if (member.getStatus() == MemberStatus.REMOVED) throw new BadRequestException("Member has been removed.");

        member.setRole(role);
        return mapToWorkspaceMemberResponse(workspaceMemberRepository.save(member));
    }

    // LOGIC XÓA THÀNH VIÊN (SOFT DELETE: REMOVED)
    @Override
    @Transactional
    @LogActivity(action = "REMOVE", entityType = "WORKSPACE_MEMBER", description = "Remove member from Workspace") 
    public void removeMemberFromWorkspace(Integer companyId, Integer workspaceId, Integer memberId) {
        // 1. Tìm thành viên
        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        // 2. Validate Hierarchy
        if (!member.getWorkspace().getId().equals(workspaceId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Member does not belong to the current workspace.");
        }
        if (!member.getWorkspace().getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new ResourceNotFoundException("Data mismatch with the current company.");
        }

        // 3. Kiểm tra nghiệp vụ: Không thể tự xóa mình
        User currentUser = securityService.getCurrentAuthenticatedUser();
        if (currentUser.getId().equals(member.getUser().getId())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("You cannot remove yourself from the workspace.");
        }

        // 4. Kiểm tra xem họ đã bị xóa chưa
        if (member.getStatus() == MemberStatus.REMOVED) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This member has already been removed from the workspace.");
        }

        // 5. Thực hiện xóa mềm (REMOVED)
        member.setStatus(MemberStatus.REMOVED);
        workspaceMemberRepository.save(member);
    }

    // ========================================================================
    // PRIVATE HELPER METHODS (MAPPERS & UTILS)
    // ========================================================================

    /**
     * Helper tạo Pageable TỔNG QUÁT.
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDir,
                                     String defaultSortField, Map<String, String> sortMapping) {

        // SortUtils giờ sẽ dùng cái map được truyền vào này để ánh xạ
        Sort sort = SortUtils.createSort(sortBy, sortDir, defaultSortField, sortMapping);
        return PageRequest.of(page, size, sort);
    }

    /**
     * Helper: Map Workspace Entity sang WorkspaceResponse DTO.
     */
    private WorkspaceResponse mapToWorkspaceResponse(Workspace kg) {
        // Lấy đường dẫn API cho ảnh bìa nếu là đường dẫn cục bộ
        String coverUrl = kg.getCoverImageUrl();
        if (coverUrl != null && !coverUrl.isBlank() && !coverUrl.startsWith("http")) {
            coverUrl = "/api/files" + coverUrl; // Giả sử FileController mapping /api/files/**
        }

        return WorkspaceResponse.builder()
                .workspaceId(kg.getId())
                .companyId(kg.getCompany().getId())
                .workspaceName(kg.getName())
                .description(kg.getDescription())
                .coverImage(coverUrl) // Sử dụng URL đã xử lý
                .color(kg.getColor())
                .createdById(kg.getCreatedBy().getId())
                .status(kg.getStatus().name())
                .createdAt(kg.getCreatedAt())
                .build();
    }

    /**
     * Helper: Map WorkspaceMember Entity sang WorkspaceMemberResponse DTO.
     */
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

    /**
     * Helper: Gửi mail thông báo khi thêm thành viên (Nội bộ).
     */
    private void sendWorkspaceNotificationEmail(User admin, User userAdded, Workspace workspace, Role role) {
        try {
            // Sửa nội dung mail sang tiếng Anh
            String workspaceUrl = String.format("%s/companies/%d/workspaces/%d", frontendUrl, workspace.getCompany().getId(), workspace.getId());
            String emailBody = String.format(
                "<p>Hello %s,</p>" +
                "<p>You have been added to the workspace <strong>%s</strong> by %s.</p>" +
                "<ul>" +
                "<li><strong>Your Role:</strong> %s</li>" +
                "<li><strong>Company:</strong> %s</li>" +
                "</ul>" +
                "<p>You can access the workspace immediately by clicking on <a href=\"%s\">this link</a>.</p>" +
                "<p>Thank you,<br>Project Management Team</p>",
                userAdded.getFullName(), workspace.getName(), admin.getFullName(), role.getRoleName(), workspace.getCompany().getName(), workspaceUrl);

            emailService.sendEmail(userAdded.getEmail(), "Added to Workspace: " + workspace.getName(), emailBody);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}