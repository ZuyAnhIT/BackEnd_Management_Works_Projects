// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/WorkspaceServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
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
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.WorkspaceService;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceStatusRequest;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository,
                                WorkspaceMemberRepository workspaceMemberRepository,
                                CompanyRepository companyRepository,
                                RoleRepository roleRepository,
                                SecurityService securityService,
                                UserRepository userRepository,
                                CompanyMemberRepository companyMemberRepository,
                                EmailService emailService) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.securityService = securityService;
        this.userRepository = userRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.emailService = emailService;
    }

    private final EmailService emailService;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    // API TAO KHONG GIAN 
    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(Integer companyId, CreateWorkspaceRequest request) { // Đã dịch
        
        // 1. Lấy thông tin người dùng và công ty
        User creator = securityService.getCurrentAuthenticatedUser(); // Đã dịch
        Company company = companyRepository.findById(companyId) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty")); // Đã dịch

        // 2. Kiểm tra nghiệp vụ (tên trùng)
        if (workspaceRepository.existsByCompany_IdAndName(companyId, request.getWorkspaceName())) { // Đã dịch
            throw new BadRequestException("Tên không gian làm việc này đã tồn tại trong công ty"); // Đã dịch
        }

        // 3. Tìm Role "WORKSPACE_ADMIN"
        Role workspaceAdminRole = roleRepository.findFirstByRoleCode(RoleCode.WORKSPACE_ADMIN.name()) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy vai trò: " + RoleCode.WORKSPACE_ADMIN.name() + ".Vui lòng cấu hình cơ sở dữ liệu." // Đã dịch
                ));

        // 4. Tạo không gian mới
        Workspace newWorkspace = Workspace.builder() // Đã dịch
                .company(company) // Đã dịch
                .name(request.getWorkspaceName()) // Đã dịch
                .description(request.getDescription()) // Đã dịch
                .coverImageUrl(request.getCoverImage()) // Đã dịch
                .color(request.getColor() != null ? request.getColor() : "#3498db") 
                .createdBy(creator) // Đã dịch
                .status(WorkspaceStatus.ACTIVE) // Đã dịch
                .build();
        
        Workspace savedWorkspace = workspaceRepository.save(newWorkspace); 

        // 5. Tự động gán người tạo làm Admin của không gian
        WorkspaceMember membership = WorkspaceMember.builder() 
                .workspace(savedWorkspace) 
                .user(creator) 
                .role(workspaceAdminRole)
                .status(MemberStatus.ACTIVE) 
                .build();
        
        workspaceMemberRepository.save(membership); 

        // 6. Map Entity sang DTO và trả về
        return mapToWorkspaceResponse(savedWorkspace);
    }


    // LOGIC HIỂN THỊ DANH SÁCH KHÔNG GIAN TRONG CÔNG TY
    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesByCompany(Integer companyId) { // Đã dịch
        // 1. Lấy danh sách Entity từ CSDL
        // (Bảo mật sẽ được xử lý ở tầng Controller bằng @PreAuthorize)
        List<Workspace> workspaces = workspaceRepository.findByCompany_Id(companyId); // Đã dịch

        // 2. Chuyển đổi (map) danh sách Entity sang danh sách DTO
        return workspaces.stream()
                .map(this::mapToWorkspaceResponse) // Tái sử dụng helper đã tạo
                .collect(Collectors.toList());
    }

    // LOGIC XEM CHI TIET PHONG BAN
    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceDetails(Integer workspaceId) {
        // Bảo mật đã được xử lý bởi @PreAuthorize ở tầng Controller.
        // Tầng service chỉ cần thực hiện logic tìm kiếm.
        
        Workspace workspace = workspaceRepository.findById(workspaceId) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc với ID: " + workspaceId)); // Đã dịch
                
        // Tái sử dụng helper đã tạo
        return mapToWorkspaceResponse(workspace);
    }


    // LOGIC MOI THANH VIEN VAO PHONG BAN
    @Override
    @Transactional
    public void inviteMemberToWorkspace(Integer companyId, Integer workspaceId, InviteWorkspaceMemberRequest request) { // Đã dịch
        
        // (Lấy admin hiện tại để biết ai là người mời)
        User admin = securityService.getCurrentAuthenticatedUser(); // Đã dịch
        String emailToInvite = request.getEmail();

        // 1. Lấy thông tin người dùng được mời
        User userToInvite = userRepository.findByEmail(emailToInvite) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy người dùng với email: " + emailToInvite // Đã dịch
                ));

        // 2. KIỂM TRA ĐIỀU KIỆN (như bạn yêu cầu)
        boolean isCompanyMember = companyMemberRepository // Đã dịch
            .existsByCompany_IdAndUser_IdAndStatus(companyId, userToInvite.getId(), MemberStatus.ACTIVE); // Đã dịch
            
        if (!isCompanyMember) {
            throw new BadRequestException(
                "Người này không phải là thành viên hoạt động của Công ty. Vui lòng liên hệ với Quản trị viên Công ty." // Đã dịch
            );
        }

        // 3. Lấy thông tin Workspace và Role
        Workspace workspace = workspaceRepository.findById(workspaceId) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc")); // Đã dịch

        // *** SỬA LOGIC: Tìm Role bằng roleCode (từ DTO) thay vì roleId ***
        Role workspaceRole = roleRepository.findFirstByRoleCode(request.getRoleCode())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found for code: " + request.getRoleCode())); // Đã dịch

        // 4. Validate Role
        if (workspaceRole.getLevel() != RoleLevel.WORKSPACE) { // Đã dịch
            throw new BadRequestException("Vai trò không hợp lệ (Không phải vai trò cấp KHÔNG GIAN)"); // Đã dịch
        }
        
        // 5. Kiểm tra xem đã là thành viên của Workspace chưa
        Optional<WorkspaceMember> existingMembership = workspaceMemberRepository // Đã dịch
            .findByWorkspace_IdAndUser_Id(workspaceId, userToInvite.getId()); // Đã dịch

        if (existingMembership.isPresent()) {
            throw new BadRequestException("Người dùng này đã là thành viên của không gian làm việc"); // Đã dịch
        }

        // 6. Thêm thành viên vào không gian
        WorkspaceMember newMembership = WorkspaceMember.builder() // Đã dịch
                .workspace(workspace) // Đã dịch
                .user(userToInvite) // Đã dịch
                .role(workspaceRole)
                .status(MemberStatus.ACTIVE) // Đã dịch
                .build();
        
        workspaceMemberRepository.save(newMembership); // Đã dịch
        
        // (LOGIC GỬI EMAIL giữ nguyên)
        sendWorkspaceNotificationEmail(admin, userToInvite, workspace, workspaceRole); // Đã dịch
    }

    // *** HÀM HELPER (Giữ nguyên) ***
    /**
     * Gửi email thông báo cho người dùng khi họ được thêm vào không gian làm việc.
     */
    private void sendWorkspaceNotificationEmail(User admin, User userAdded, Workspace workspace, Role role) { // Đã dịch
        try {
            // Tạo link chi tiết
            String workspaceUrl = String.format("%s/companies/%d/workspaces/%d", 
                frontendUrl, 
                workspace.getCompany().getId(), // Đã dịch
                workspace.getId()); // Đã dịch

            String emailBody = String.format(
                "<p>Xin chào %s,</p>" +
                "<p>Bạn vừa được thêm vào không gian làm việc <strong>%s</strong> bởi %s.</p>" +
                "<ul>" +
                "<li><strong>Vai trò của bạn:</strong> %s</li>" +
                "<li><strong>Công ty:</strong> %s</li>" +
                "</ul>" +
                "<p>Bạn có thể truy cập không gian làm việc ngay bằng cách nhấp vào <a href=\"%s\">liên kết này</a>.</p>" +
                "<p>Cảm ơn,<br>Đội ngũ Quản lý Dự án</p>",
                userAdded.getFullName(), // Đã dịch
                admin.getFullName(), // Đã dịch
                workspace.getName(), // Đã dịch
                role.getRoleName(), // Đã dịch
                workspace.getCompany().getName(), // Đã dịch
                workspaceUrl
            );

            emailService.sendEmail(
                userAdded.getEmail(), 
                String.format("Bạn đã được thêm vào không gian làm việc: %s", workspace.getName()), // Đã dịch
                emailBody
            );

        } catch (Exception e) {
            // (Nên log lỗi này)
            System.err.println("Lỗi khi gửi email thông báo không gian làm việc: " + e.getMessage()); // Đã dịch
            // Không ném lỗi ra ngoài để không làm hỏng giao dịch chính
        }
    }


    /**
     * Hàm helper để chuyển đổi Entity KhongGian sang WorkspaceResponse DTO.
     * @param kg Entity KhongGian
     * @return WorkspaceResponse DTO
     */
    private WorkspaceResponse mapToWorkspaceResponse(Workspace kg) { // Đã dịch
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

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(Integer workspaceId, UpdateWorkspaceRequest request) {

        // 1. Tìm Workspace
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc với ID: " + workspaceId));

        // 2. Xử lý logic cập nhật tên (Nếu có)
        if (request.getName() != null && !request.getName().isEmpty()
                && !Objects.equals(request.getName(), workspace.getName())) {

            // Kiểm tra tên mới có bị trùng trong CÙNG CÔNG TY không
            Optional<Workspace> existing = workspaceRepository.findByCompany_IdAndName(
                    workspace.getCompany().getId(), // Lấy ID công ty từ workspace
                    request.getName()
            );

            // Chỉ ném lỗi nếu tìm thấy một workspace KHÁC có CÙNG TÊN
            if (existing.isPresent() && !existing.get().getId().equals(workspace.getId())) {
                throw new BadRequestException("Tên không gian làm việc đã tồn tại trong công ty này");
            }

            // Nếu không trùng, cập nhật tên mới
            workspace.setName(request.getName());
        }

        // 3. Cập nhật các trường khác (nếu chúng được cung cấp)
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }
        if (request.getCoverImage() != null) {
            // Khớp tên trường 'coverImage' từ DTO với 'coverImageUrl' trong Entity
            workspace.setCoverImageUrl(request.getCoverImage());
        }
        if (request.getColor() != null) {
            workspace.setColor(request.getColor());
        }

        // 4. Lưu vào CSDL
        Workspace updatedWorkspace = workspaceRepository.save(workspace);

        // 5. Map sang DTO và trả về (sử dụng helper có sẵn của bạn)
        return mapToWorkspaceResponse(updatedWorkspace);
    }

    /**
     * LOGIC XÓA MỀM WORKSPACE
     */
    @Override
    @Transactional
    public void deleteWorkspace(Integer workspaceId) {

        // 1. Tìm Workspace
        // Bảo mật (ai được phép gọi) đã được xử lý bởi @PreAuthorize
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc với ID:" + workspaceId));

        // 2. Kiểm tra nghiệp vụ: Nếu đã xóa rồi thì báo lỗi
        if (workspace.getStatus() == WorkspaceStatus.DELETED) {
            throw new BadRequestException("Không gian làm việc này đã bị xóa");
        }

        // 3. Thực hiện Xóa Mềm
        workspace.setStatus(WorkspaceStatus.DELETED);

        // 4. Lưu lại
        workspaceRepository.save(workspace);
    }
    

    // LOGIC LAY DANH SACH THANH VIEN KHONG GIAN
    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getWorkspaceMembers(Integer workspaceId) {
        // Bảo mật đã được xử lý ở Controller
        
        // 1. Lấy danh sách thành viên từ CSDL
        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspace_Id(workspaceId);

        // 2. Map sang DTO
        return members.stream()
                .map(this::mapToWorkspaceMemberResponse) // Dùng helper mới
                .collect(Collectors.toList());
    }

    // *** THÊM HÀM HELPER NÀY ***
    /**
     * Hàm helper để chuyển đổi WorkspaceMember (Entity) sang WorkspaceMemberResponse (DTO).
     */
    private WorkspaceMemberResponse mapToWorkspaceMemberResponse(WorkspaceMember member) {
        return WorkspaceMemberResponse.builder()
                .memberId(member.getId())
                .userId(member.getUser().getId())
                .fullName(member.getUser().getFullName())
                .email(member.getUser().getEmail())
                .avatarUrl(member.getUser().getAvatarUrl())
                .roleName(member.getRole().getRoleName())
                .joinedAt(member.getJoinedAt())
                .status(member.getStatus())
                .build();
    }

    // LOGIC XEM CHI TIET THANH VIEN KHONG GIAN
    @Override
    @Transactional(readOnly = true)
    public WorkspaceMemberResponse getWorkspaceMemberDetails(Integer workspaceId, Integer memberId) {
        // Bảo mật (người gọi có phải là thành viên không) đã được xử lý ở Controller.
        
        // 1. Tìm thành viên bằng ID
        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên không gian làm việc với ID: " + memberId)); // Đã dịch

        // 2. KIỂM TRA BẢO MẬT (IDOR): 
        // Đảm bảo bản ghi 'memberId' này thực sự thuộc về 'workspaceId'
        if (!member.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Không tìm thấy thành viên trong không gian làm việc này"); // Đã dịch
        }

        // 3. Map và trả về (tái sử dụng helper)
        return mapToWorkspaceMemberResponse(member);
    }

    // LOGIC CAP NHAT TRANG THAI THANH VIEN KHONG GIAN
    @Override
    @Transactional
    public WorkspaceMemberResponse updateWorkspaceMemberStatus(Integer companyId, Integer workspaceId, Integer memberId, UpdateMemberStatusRequest request) {
        // 1. Lấy thông tin thành viên
        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên không gian làm việc với ID: " + memberId)); // Đã dịch

        // 2. Kiểm tra bảo mật (IDOR): Đảm bảo thành viên này thuộc đúng không gian
        if (!member.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Không tìm thấy thành viên trong không gian làm việc này"); // Đã dịch
        }
        
        // 3. Kiểm tra bảo mật (IDOR): Đảm bảo không gian này thuộc đúng công ty
        if (!member.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy không gian làm việc trong công ty này"); // Đã dịch
        }

        // 4. Kiểm tra nghiệp vụ: Không cho phép đổi status của chính mình
        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) {
            throw new BadRequestException("Bạn không thể thay đổi trạng thái của mình."); // Đã dịch
        }
        
        // 5. Kiểm tra nghiệp vụ: Không cho phép dùng API này để "Xóa" (REMOVED)
        MemberStatus newStatus = request.getNewStatus();
        if (newStatus == MemberStatus.REMOVED) {
            throw new BadRequestException("Vui lòng sử dụng endpoint 'Remove Member' để loại bỏ thành viên, không phải endpoint cập nhật trạng thái này."); // Đã dịch
        }
        // 5.1. Kiểm tra nếu trạng thái mới trùng với trạng thái hiện tại
if (member.getStatus() == newStatus) {
    throw new BadRequestException(
        "Trạng thái mới giống với trạng thái hiện tại. Không có gì để cập nhật."
    );
}
        // 5.2. Không cho phép thay đổi trạng thái nếu thành viên đã bị REMOVE
if (member.getStatus() == MemberStatus.REMOVED) {
    throw new BadRequestException("Không thể thay đổi trạng thái vì thành viên này đã bị REMOVE.");
}

        // 6. Cập nhật trạng thái
        member.setStatus(newStatus);
        WorkspaceMember updatedMember = workspaceMemberRepository.save(member);

        // 7. Trả về DTO đã cập nhật (tái sử dụng helper)
        return mapToWorkspaceMemberResponse(updatedMember);
    }

    // LOGIC CAP NHAT TRANG THAI KHONG GIAN
    @Override
    @Transactional
    public WorkspaceResponse updateWorkspaceStatus(Integer companyId, Integer workspaceId, UpdateWorkspaceStatusRequest request) {
        // Bảo mật (ai có quyền) đã được xử lý ở Controller.
        
        // 1. Lấy thông tin không gian
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc với ID: " + workspaceId)); // Đã dịch

        // 2. Kiểm tra bảo mật (IDOR): Đảm bảo không gian này thuộc đúng công ty
        if (!workspace.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy không gian làm việc trong công ty này"); // Đã dịch
        }
        
        // 3. Kiểm tra nghiệp vụ (ví dụ: không cho phép thay đổi trạng thái giống hệt)
        if (workspace.getStatus() == request.getNewStatus()) {
            throw new BadRequestException("Không gian làm việc đã ở trạng thái yêu cầu."); // Đã dịch
        }

        // 4. Cập nhật trạng thái
        workspace.setStatus(request.getNewStatus());
        Workspace updatedWorkspace = workspaceRepository.save(workspace);

        // 5. Trả về DTO (tái sử dụng helper)
        return mapToWorkspaceResponse(updatedWorkspace);
    }

    // LOGIC CAP NHAT VAI TRO THANH VIEN KHONG GIAN
    @Override
    @Transactional
    public WorkspaceMemberResponse updateWorkspaceMemberRole(Integer companyId, Integer workspaceId, Integer memberId, String newRoleCode) {
        // 1. Lấy thông tin thành viên
        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên không gian làm việc với ID: " + memberId));

        // 2. Kiểm tra bảo mật (IDOR): Đảm bảo thành viên này thuộc đúng không gian
        if (!member.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Không tìm thấy thành viên trong không gian làm việc này");
        }

        // 3. Kiểm tra bảo mật (IDOR): Đảm bảo không gian này thuộc đúng công ty
        if (!member.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy không gian làm việc trong công ty này");
        }

        // 4. Kiểm tra nghiệp vụ: Không cho phép đổi vai trò của chính mình
        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) {
            throw new BadRequestException("Bạn không thể thay đổi vai trò của chính mình.");
        }

        // 5. Tìm vai trò (Role) mới
        Role newRole = roleRepository.findFirstByRoleCode(newRoleCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với mã: " + newRoleCode));

        // 6. Kiểm tra nghiệp vụ: Đảm bảo vai trò mới là CẤP KHÔNG GIAN
        if (newRole.getLevel() != RoleLevel.WORKSPACE) {
            throw new BadRequestException("Vai trò không hợp lệ (Không phải vai trò cấp KHÔNG GIAN)");
        }
        // 6.1 Kiểm tra vai trò mới có trùng vai trò hiện tại không
        if (member.getRole().getRoleCode().equals(newRoleCode)) {
            throw new BadRequestException("Vai trò mới giống với vai trò hiện tại. Không có gì để cập nhật.");
        }
        // 6.2. Không cho phép thay đổi vai trò nếu thành viên đã bị REMOVE
if (member.getStatus() == MemberStatus.REMOVED) {
    throw new BadRequestException("Không thể thay đổi vai trò vì thành viên này đã bị REMOVE.");
}

        // 7. Cập nhật vai trò
        member.setRole(newRole);
        WorkspaceMember updatedMember = workspaceMemberRepository.save(member);

        // 8. Trả về DTO đã cập nhật (tái sử dụng helper)
        return mapToWorkspaceMemberResponse(updatedMember);
    }
    // LOGIC XOA THANH VIEN KHOI WORKSPACE
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
