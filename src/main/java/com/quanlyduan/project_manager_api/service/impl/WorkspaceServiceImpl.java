// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/WorkspaceServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
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
import com.quanlyduan.project_manager_api.service.SecurityService; 
import com.quanlyduan.project_manager_api.service.WorkspaceService;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceRequest;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository; // Đã dịch
    private final WorkspaceMemberRepository workspaceMemberRepository; // Đã dịch
    private final CompanyRepository companyRepository; // Đã dịch
    private final RoleRepository roleRepository;
    private final SecurityService securityService; 
    private final UserRepository userRepository; // Đã dịch
    private final CompanyMemberRepository companyMemberRepository; // Đã dịch

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
                .orElseThrow(() -> new ResourceNotFoundException("Company not found")); // Đã dịch

        // 2. Kiểm tra nghiệp vụ (tên trùng)
        if (workspaceRepository.existsByCompany_IdAndName(companyId, request.getWorkspaceName())) { // Đã dịch
            throw new BadRequestException("This workspace name already exists in the company"); // Đã dịch
        }

        // 3. Tìm Role "WORKSPACE_ADMIN"
        Role workspaceAdminRole = roleRepository.findFirstByRoleCode(RoleCode.WORKSPACE_ADMIN.name()) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Role not found: " + RoleCode.WORKSPACE_ADMIN.name() + ". Please configure the database." // Đã dịch
                ));

        // 4. Tạo không gian mới
        Workspace newWorkspace = Workspace.builder() // Đã dịch
                .company(company) // Đã dịch
                .name(request.getWorkspaceName()) // Đã dịch
                .description(request.getDescription()) // Đã dịch
                .coverImageUrl(request.getCoverImage()) // Đã dịch
                .color(request.getColor() != null ? request.getColor() : "#3498db") // Đã dịch
                .createdBy(creator) // Đã dịch
                .status(WorkspaceStatus.ACTIVE) // Đã dịch
                .build();
        
        Workspace savedWorkspace = workspaceRepository.save(newWorkspace); // Đã dịch

        // 5. Tự động gán người tạo làm Admin của không gian
        WorkspaceMember membership = WorkspaceMember.builder() // Đã dịch
                .workspace(savedWorkspace) // Đã dịch
                .user(creator) // Đã dịch
                .role(workspaceAdminRole)
                .status(MemberStatus.ACTIVE) // Đã dịch
                .build();
        
        workspaceMemberRepository.save(membership); // Đã dịch

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
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId)); // Đã dịch
                
        // Tái sử dụng helper đã tạo
        return mapToWorkspaceResponse(workspace);
    }


    // LOGIC MOI THANH VIEN VAO PHONG BAN
    @Override
    @Transactional
    public void inviteMemberToWorkspace(Integer companyId, Integer workspaceId, InviteWorkspaceMemberRequest request) { // Đã dịch
        
        // *** THÊM DÒNG NÀY *** (Lấy admin hiện tại để biết ai là người mời)
        User admin = securityService.getCurrentAuthenticatedUser(); // Đã dịch
        String emailToInvite = request.getEmail();

        // 1. Lấy thông tin người dùng được mời
        User userToInvite = userRepository.findByEmail(emailToInvite) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with email: " + emailToInvite // Đã dịch
                ));

        // 2. KIỂM TRA ĐIỀU KIỆN (như bạn yêu cầu)
        // Sửa: Chỉ kiểm tra thành viên ACTIVE
        boolean isCompanyMember = companyMemberRepository
            .existsByCompany_IdAndUser_IdAndStatus(companyId, userToInvite.getId(), MemberStatus.ACTIVE); // Sửa
            
        if (!isCompanyMember) {
            throw new BadRequestException(
                "This person is not an active member of the Company. Please contact the Company Admin." // Đã dịch
            );
        }

        // 3. Lấy thông tin Workspace và Role
        Workspace workspace = workspaceRepository.findById(workspaceId) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found")); // Đã dịch

        Role workspaceRole = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found")); // Đã dịch

        // 4. Validate Role
        if (workspaceRole.getLevel() != RoleLevel.WORKSPACE) { // Đã dịch
            throw new BadRequestException("Invalid role (Not a WORKSPACE level role)"); // Đã dịch
        }
        
        // 5. Kiểm tra xem đã là thành viên của Workspace chưa
        Optional<WorkspaceMember> existingMembership = workspaceMemberRepository // Đã dịch
            .findByWorkspace_IdAndUser_Id(workspaceId, userToInvite.getId()); // Đã dịch

        if (existingMembership.isPresent()) {
            throw new BadRequestException("This user is already a member of the workspace"); // Đã dịch
        }

        // 6. Thêm thành viên vào không gian
        WorkspaceMember newMembership = WorkspaceMember.builder() // Đã dịch
                .workspace(workspace) // Đã dịch
                .user(userToInvite) // Đã dịch
                .role(workspaceRole)
                .status(MemberStatus.ACTIVE) // Đã dịch
                .build();
        
        workspaceMemberRepository.save(newMembership); // Đã dịch
        
        // *** LOGIC GỬI EMAIL ***
        sendWorkspaceNotificationEmail(admin, userToInvite, workspace, workspaceRole); // Đã dịch
    }

    // *** HÀM HELPER  ***
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
                "<p>Hi %s,</p>" + // Đã dịch
                "<p>You have just been added to the workspace <strong>%s</strong> by %s.</p>" + // Đã dịch
                "<ul>" +
                "<li><strong>Your role:</strong> %s</li>" + // Đã dịch
                "<li><strong>Company:</strong> %s</li>" + // Đã dịch
                "</ul>" +
                "<p>You can access the workspace now by clicking <a href=\"%s\">this link</a>.</p>" + // Đã dịch
                "<p>Thanks,<br>The Project Manager Team</p>", // Đã dịch
                userAdded.getFullName(), // Đã dịch
                admin.getFullName(), // Đã dịch
                workspace.getName(), // Đã dịch
                role.getRoleName(), // Đã dịch
                workspace.getCompany().getName(), // Đã dịch
                workspaceUrl
            );

            emailService.sendEmail(
                userAdded.getEmail(), 
                String.format("You have been added to the workspace: %s", workspace.getName()), // Đã dịch
                emailBody
            );

        } catch (Exception e) {
            // (Nên log lỗi này)
            System.err.println("Error sending workspace notification email: " + e.getMessage()); // Đã dịch
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
                .workspaceId(kg.getId()) // Đã dịch
                .companyId(kg.getCompany().getId()) // Lấy ID an toàn // Đã dịch
                .workspaceName(kg.getName()) // Đã dịch
                .description(kg.getDescription()) // Đã dịch
                .coverImage(kg.getCoverImageUrl()) // Đã dịch
                .color(kg.getColor()) // Đã dịch
                .createdById(kg.getCreatedBy().getId()) // Lấy ID an toàn // Đã dịch
                .status(kg.getStatus().name()) // Trả về tên Enum (String) // Đã dịch
                .createdAt(kg.getCreatedAt()) // Đã dịch
                .build();
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(Integer workspaceId, UpdateWorkspaceRequest request) {

        // 1. Tìm Workspace
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));

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
                throw new BadRequestException("Workspace name already exists in this company");
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
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));

        // 2. Kiểm tra nghiệp vụ: Nếu đã xóa rồi thì báo lỗi
        if (workspace.getStatus() == WorkspaceStatus.DELETED) {
            throw new BadRequestException("This workspace has already been deleted");
        }

        // 3. Thực hiện Xóa Mềm
        workspace.setStatus(WorkspaceStatus.DELETED);

        // 4. Lưu lại
        workspaceRepository.save(workspace);
    }

}