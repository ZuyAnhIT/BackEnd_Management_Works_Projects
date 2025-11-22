// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/WorkspaceServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

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
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.repository.*;
import com.quanlyduan.project_manager_api.repository.specification.WorkspaceMemberSpecification;
import com.quanlyduan.project_manager_api.repository.specification.WorkspaceSpecification;
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.FileStorageService;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.WorkspaceService;
import com.quanlyduan.project_manager_api.util.SortUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

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
    // NHÓM 1: QUẢN LÝ WORKSPACE (Create, Update, List...)
    // ========================================================================

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(Integer companyId, CreateWorkspaceRequest request, MultipartFile coverImageFile) {
        
        User creator = securityService.getCurrentAuthenticatedUser();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        if (workspaceRepository.existsByCompany_IdAndName(companyId, request.getWorkspaceName())) {
            throw new BadRequestException("Tên không gian làm việc này đã tồn tại trong công ty");
        }

        Role workspaceAdminRole = roleRepository.findFirstByRoleCode(RoleCode.WORKSPACE_ADMIN.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy vai trò: WORKSPACE_ADMIN. Vui lòng cấu hình cơ sở dữ liệu."
                ));

        Workspace newWorkspace = Workspace.builder()
                .company(company)
                .name(request.getWorkspaceName())
                .description(request.getDescription())
                .color(request.getColor() != null ? request.getColor() : "#3498db") 
                .createdBy(creator)
                .status(WorkspaceStatus.ACTIVE)
                .build();

        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String coverPath = fileStorageService.storeFile(coverImageFile, "workspace-covers");
            newWorkspace.setCoverImageUrl(coverPath);
        } else if (request.getCoverImage() != null) {
            newWorkspace.setCoverImageUrl(request.getCoverImage());
        }
        
        Workspace savedWorkspace = workspaceRepository.save(newWorkspace); 

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

        // 2. GỌI HELPER CHUNG (Truyền map vào)
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "createdAt", workspaceMapping);

        Page<Workspace> workspacePage = workspaceRepository.findByCompany_Id(companyId, pageable);
        Page<WorkspaceResponse> dtoPage = workspacePage.map(this::mapToWorkspaceResponse);
        return new PageResponseDTO<>(dtoPage);
    }
    
    // (Logic tìm kiếm nâng cao cho Workspace - nếu cần)
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

        Specification<Workspace> spec = WorkspaceSpecification.filterWorkspaces(
            companyId, searchName, searchCode, searchDescription, searchStatus
        );

        Page<Workspace> workspacePage = workspaceRepository.findAll(spec, pageable);
        Page<WorkspaceResponse> dtoPage = workspacePage.map(this::mapToWorkspaceResponse);
        return new PageResponseDTO<>(dtoPage);
    }


    // ========================================================================
    // NHÓM 2: QUẢN LÝ THÀNH VIÊN WORKSPACE (Members)
    // ========================================================================

    // LOGIC LAY DANH SACH THANH VIEN (CO BAN)
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<WorkspaceMemberResponse> getWorkspaceMembers(
            Integer workspaceId, int page, int size, String sortBy, String sortDir) {
        
        // 1. ĐỊNH NGHĨA MAP CHO MEMBER (Khác với Workspace)
        Map<String, String> memberMapping = Map.of(
            "joinedAt", "joinedAt",          // Đúng tên Entity
            "createdAt", "joinedAt",         // Alias: Nếu gửi createdAt -> hiểu là joinedAt
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "phone", "user.phoneNumber"
        );

        // 2. GỌI HELPER CHUNG (Truyền map của Member vào)
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", memberMapping);

        Page<WorkspaceMember> membersPage = workspaceMemberRepository.findByWorkspace_Id(workspaceId, pageable);
        Page<WorkspaceMemberResponse> dtoPage = membersPage.map(this::mapToWorkspaceMemberResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // LOGIC TIM KIEM THANH VIEN (NANG CAO)
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
        
        // 2. Gọi Helper chung
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", memberMapping);

        // 3. Specification
        Specification<WorkspaceMember> spec = WorkspaceMemberSpecification.filterMembers(
            workspaceId, searchName, searchEmail, searchRoleName, searchPhone
        );

        Page<WorkspaceMember> membersPage = workspaceMemberRepository.findAll(spec, pageable);
        Page<WorkspaceMemberResponse> dtoPage = membersPage.map(this::mapToWorkspaceMemberResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // ========================================================================
    // PRIVATE HELPER METHODS (ĐÃ SỬA ĐỂ LINH HOẠT)
    // ========================================================================

    /**
     * Helper tạo Pageable TỔNG QUÁT.
     * @param defaultSortField Trường sort mặc định nếu client không gửi (vd: "createdAt" hoặc "joinedAt")
     * @param sortMapping Map ánh xạ tên trường cụ thể cho từng loại đối tượng
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDir, 
                                    String defaultSortField, Map<String, String> sortMapping) {
        
        // SortUtils giờ sẽ dùng cái map được truyền vào này để ánh xạ
        Sort sort = SortUtils.createSort(sortBy, sortDir, defaultSortField, sortMapping);
        return PageRequest.of(page, size, sort);
    }

    private WorkspaceResponse mapToWorkspaceResponse(Workspace kg) {
        return WorkspaceResponse.builder()
                .workspaceId(kg.getId())
                .companyId(kg.getCompany().getId())
                .workspaceName(kg.getName())
                .description(kg.getDescription())
                .coverImage(kg.getCoverImageUrl())
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
    
    private void sendWorkspaceNotificationEmail(User admin, User userAdded, Workspace workspace, Role role) {
         try {
            String workspaceUrl = String.format("%s/companies/%d/workspaces/%d", frontendUrl, workspace.getCompany().getId(), workspace.getId());
            String emailBody = String.format(
                "<p>Xin chào %s,</p>" +
                "<p>Bạn vừa được thêm vào không gian làm việc <strong>%s</strong> bởi %s.</p>" +
                "<ul>" +
                "<li><strong>Vai trò của bạn:</strong> %s</li>" +
                "<li><strong>Công ty:</strong> %s</li>" +
                "</ul>" +
                "<p>Bạn có thể truy cập không gian làm việc ngay bằng cách nhấp vào <a href=\"%s\">liên kết này</a>.</p>" +
                "<p>Cảm ơn,<br>Đội ngũ Quản lý Dự án</p>",
                userAdded.getFullName(), workspace.getName(), admin.getFullName(), role.getRoleName(), workspace.getCompany().getName(), workspaceUrl);
            
            emailService.sendEmail(userAdded.getEmail(), "Thêm vào không gian làm việc: " + workspace.getName(), emailBody);
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail: " + e.getMessage());
        }
    }


    // ... (Các hàm logic nghiệp vụ khác)
    
    @Override
    public WorkspaceResponse getWorkspaceDetails(Integer workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc"));
        return mapToWorkspaceResponse(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(Integer workspaceId, UpdateWorkspaceRequest request, MultipartFile coverImageFile) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc"));
        
        if (request.getName() != null && !request.getName().equals(workspace.getName())) {
             if (workspaceRepository.existsByCompany_IdAndName(workspace.getCompany().getId(), request.getName())) {
                  throw new BadRequestException("Tên không gian đã tồn tại");
             }
             workspace.setName(request.getName());
        }
        if (request.getDescription() != null) workspace.setDescription(request.getDescription());
        if (request.getColor() != null) workspace.setColor(request.getColor());
        
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String path = fileStorageService.storeFile(coverImageFile, "workspace-covers");
            workspace.setCoverImageUrl(path);
        } else if (request.getCoverImage() != null) {
            workspace.setCoverImageUrl(request.getCoverImage());
        }
        
        return mapToWorkspaceResponse(workspaceRepository.save(workspace));
    }

    @Override
    @Transactional
    public void deleteWorkspace(Integer workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc"));
        if (workspace.getStatus() == WorkspaceStatus.DELETED) throw new BadRequestException("Đã bị xóa");
        workspace.setStatus(WorkspaceStatus.DELETED);
        workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public void inviteMemberToWorkspace(Integer companyId, Integer workspaceId, InviteWorkspaceMemberRequest request) {
        User admin = securityService.getCurrentAuthenticatedUser();
        User userToInvite = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
        
        if (!companyMemberRepository.existsByCompany_IdAndUser_IdAndStatus(companyId, userToInvite.getId(), MemberStatus.ACTIVE)) {
            throw new BadRequestException("Người này chưa là thành viên hoạt động của công ty");
        }
        
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow();
        Role role = roleRepository.findFirstByRoleCode(request.getRoleCode()).orElseThrow();
        
        if (workspaceMemberRepository.findByWorkspace_IdAndUser_Id(workspaceId, userToInvite.getId()).isPresent()) {
            throw new BadRequestException("Đã là thành viên của không gian này");
        }
        
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace).user(userToInvite).role(role).status(MemberStatus.ACTIVE).build();
        workspaceMemberRepository.save(member);
        sendWorkspaceNotificationEmail(admin, userToInvite, workspace, role);
    }

    @Override
    public WorkspaceMemberResponse getWorkspaceMemberDetails(Integer workspaceId, Integer memberId) {
        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên"));
        if (!member.getWorkspace().getId().equals(workspaceId)) throw new ResourceNotFoundException("Sai không gian làm việc");
        return mapToWorkspaceMemberResponse(member);
    }

    @Override
    @Transactional
    public WorkspaceMemberResponse updateWorkspaceMemberStatus(Integer companyId, Integer workspaceId, Integer memberId, UpdateMemberStatusRequest request) {
         WorkspaceMember member = workspaceMemberRepository.findById(memberId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên"));
         
         if (!member.getWorkspace().getId().equals(workspaceId)) throw new ResourceNotFoundException("Sai không gian làm việc");
         if (!member.getWorkspace().getCompany().getId().equals(companyId)) throw new ResourceNotFoundException("Sai công ty");

         User admin = securityService.getCurrentAuthenticatedUser();
         if (admin.getId().equals(member.getUser().getId())) throw new BadRequestException("Không thể tự đổi trạng thái");

         if (request.getNewStatus() == MemberStatus.REMOVED) throw new BadRequestException("Dùng API xóa để xóa");
         if (member.getStatus() == request.getNewStatus()) throw new BadRequestException("Trạng thái không thay đổi");

         
         member.setStatus(request.getNewStatus());
         return mapToWorkspaceMemberResponse(workspaceMemberRepository.save(member));
    }

    @Override
    @Transactional
    public WorkspaceMemberResponse updateWorkspaceMemberRole(Integer companyId, Integer workspaceId, Integer memberId, String newRoleCode) {
        WorkspaceMember member = workspaceMemberRepository.findById(memberId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên"));
        
        if (!member.getWorkspace().getId().equals(workspaceId)) throw new ResourceNotFoundException("Sai không gian làm việc");
        if (!member.getWorkspace().getCompany().getId().equals(companyId)) throw new ResourceNotFoundException("Sai công ty");

        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) throw new BadRequestException("Không thể tự đổi vai trò");

        Role role = roleRepository.findFirstByRoleCode(newRoleCode).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò"));
        if (role.getLevel() != RoleLevel.WORKSPACE) throw new BadRequestException("Vai trò không hợp lệ");
        
        if (member.getRole().getRoleCode().equals(newRoleCode)) throw new BadRequestException("Vai trò không thay đổi");
        if (member.getStatus() == MemberStatus.REMOVED) throw new BadRequestException("Thành viên đã bị xóa");

        member.setRole(role);
        return mapToWorkspaceMemberResponse(workspaceMemberRepository.save(member));
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspaceStatus(Integer companyId, Integer workspaceId, UpdateWorkspaceStatusRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy workspace"));
        if (!workspace.getCompany().getId().equals(companyId)) throw new ResourceNotFoundException("Sai công ty");
        if (workspace.getStatus() == request.getNewStatus()) throw new BadRequestException("Trạng thái không thay đổi");
        
        workspace.setStatus(request.getNewStatus());
        return mapToWorkspaceResponse(workspaceRepository.save(workspace));
    }

    // *** HÀM MỚI: Xóa thành viên khỏi Workspace ***
    @Override
    @Transactional
    public void removeMemberFromWorkspace(Integer companyId, Integer workspaceId, Integer memberId) {
        // 1. Tìm thành viên trực tiếp bằng memberId
        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên với ID: " + memberId));

        // 2. Validate: Đảm bảo member này thuộc đúng workspace đang thao tác
        if (!member.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Thành viên này không thuộc không gian làm việc hiện tại.");
        }

        // 3. Validate: Đảm bảo workspace thuộc đúng company
        if (!member.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Dữ liệu không khớp với công ty hiện tại.");
        }

        // 4. Kiểm tra xem có tự xóa chính mình không (Lấy User từ Member)
        User currentUser = securityService.getCurrentAuthenticatedUser();
        if (currentUser.getId().equals(member.getUser().getId())) {
            throw new BadRequestException("Bạn không thể tự xóa mình khỏi không gian làm việc.");
        }

        // 5. Kiểm tra xem họ đã bị xóa chưa
        if (member.getStatus() == MemberStatus.REMOVED) {
            throw new BadRequestException("Thành viên này đã bị xóa khỏi không gian làm việc.");
        }

        // 6. Thực hiện xóa mềm
        member.setStatus(MemberStatus.REMOVED);
        workspaceMemberRepository.save(member);
    }
}