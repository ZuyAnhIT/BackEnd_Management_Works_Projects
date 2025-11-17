// File: src/main/java/com/quanlyduan/project_manager_api/security/SecurityServiceImpl.java
package com.quanlyduan.project_manager_api.security;

import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import com.quanlyduan.project_manager_api.repository.*;
import com.quanlyduan.project_manager_api.security.SecurityService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quanlyduan.project_manager_api.model.Sprint; 

@Service("securityService") // Đặt tên Bean là "securityService" (thay vì securityServicePermission)
@Transactional(readOnly = true) // Các hàm kiểm tra quyền chỉ đọc
public class SecurityServiceImpl implements SecurityService {

    // === TẤT CẢ REPOSITORIES CẦN THIẾT ===
    private final CompanyMemberRepository companyMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository; // Cần cho getCurrentAuthenticatedUser
    private final TaskRepository taskRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;

    // === CONSTRUCTOR THỦ CÔNG (THEO YÊU CẦU) ===
    public SecurityServiceImpl(CompanyMemberRepository companyMemberRepository,
                               WorkspaceMemberRepository workspaceMemberRepository,
                               ProjectMemberRepository projectMemberRepository,
                               UserRoleRepository userRoleRepository,
                               UserRepository userRepository,
                               TaskRepository taskRepository,
                               WorkspaceRepository workspaceRepository,
                               ProjectRepository projectRepository,
                               SprintRepository sprintRepository) {
        this.companyMemberRepository = companyMemberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.workspaceRepository = workspaceRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
    }

    // =============================================
    // KHỐI LOGIC 1: LẤY THÔNG TIN NGƯỜI DÙNG HIỆN TẠI
    // =============================================

    /**
     * Helper: Lấy UserPrincipal (chứa ID, Email) từ Context bảo mật.
     * Trả về null nếu không ai đăng nhập.
     */
    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            // Ném lỗi hoặc trả về null tùy logic. Trả về null an toàn hơn.
            return null;
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    @Override
    public String getCurrentUserEmail() {
        UserPrincipal user = getCurrentUserPrincipal();
        return (user != null) ? user.getEmail() : null;
    }

    @Override
    public Integer getCurrentUserId() {
        UserPrincipal user = getCurrentUserPrincipal();
        return (user != null) ? user.getId() : null;
    }
    
    /**
     * Helper (từ file cũ): Lấy toàn bộ Entity User (NguoiDung)
     * Dùng cho các service cần đối tượng User đầy đủ.
     */
    @Override
    public User getCurrentAuthenticatedUser() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
             throw new UsernameNotFoundException("Không tìm thấy thông tin người dùng đã xác thực.");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));
    }

    // =============================================
    // KHỐI LOGIC 2: HÀM TIỆN ÍCH TRUY VẤN NGƯỢC
    // =============================================

    /**
     * Helper: Tìm CompanyID từ WorkspaceID
     */
    private Integer getCompanyIdFromWorkspace(Integer workspaceId) {
        // Truy vấn workspace để lấy companyId
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Workspace."))
                .getCompany().getId();
    }

    /**
     * Helper: Tìm WorkspaceID từ ProjectID
     */
    private Integer getWorkspaceIdFromProject(Integer projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Project."))
                .getWorkspace().getId();
    }

    // =============================================
    // KHỐI LOGIC 3: KIỂM TRA QUYỀN HẠN (PERMISSION-BASED)
    // =============================================

    @Override
    public boolean hasSystemPermission(String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null)
            return false;
        // Gọi repo chính xác
        return userRoleRepository.checkSystemPermission(userId, permissionCode);
    }
    
    @Override
    public boolean hasCompanyPermission(Integer companyId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || companyId == null)
            return false;
        // Gọi repo chính xác
        return companyMemberRepository.checkCompanyPermission(userId, companyId, permissionCode);
    }

    @Override
    public boolean hasWorkspacePermission(Integer workspaceId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || workspaceId == null)
            return false;
        // Gọi repo chính xác
        return workspaceMemberRepository.checkWorkspacePermission(userId, workspaceId, permissionCode);
    }
    
    @Override
    public boolean hasProjectPermission(Integer projectId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || projectId == null)
            return false;
        // Gọi repo chính xác
        return projectMemberRepository.checkProjectPermission(userId, projectId, permissionCode);
    }
    
    @Override
    public boolean hasTaskPermission(Integer taskId, String permissionCode) {
        // Hàm này không đổi, logic vẫn đúng
        Integer userId = getCurrentUserId();
        if (userId == null || taskId == null) return false;
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Task để kiểm tra quyền."));
        Integer projectId = task.getProject().getId();
        // Gọi hàm hasPermission (bên dưới) để kiểm tra theo tầng (Project > Workspace > Company)
        return hasPermission("project", projectId, permissionCode);
    }

    /**
     * LOGIC KIỂM TRA THỪA KẾ QUYỀN (QUAN TRỌNG)
     * Kiểm tra quyền từ cấp thấp (Project) lên cấp cao (Company).
     * Ví dụ: Nếu bạn là COMPANY_ADMIN, bạn sẽ tự động có quyền trên Project.
     */
    @Override
    public boolean hasPermission(String scope, Integer targetId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || targetId == null || scope == null || permissionCode == null)
            return false;

        switch (scope.toLowerCase()) {
            case "company":
                // 1. Chỉ kiểm tra quyền công ty
                return companyMemberRepository.checkCompanyPermission(userId, targetId, permissionCode);

            case "workspace":
                // 1. Kiểm tra quyền trực tiếp trên Workspace
                boolean hasWorkspacePerm = workspaceMemberRepository.checkWorkspacePermission(userId, targetId, permissionCode);
                if (hasWorkspacePerm) return true;

                // 2. (Thừa kế) Kiểm tra quyền trên Company cha
                Integer companyId = getCompanyIdFromWorkspace(targetId);
                return companyMemberRepository.checkCompanyPermission(userId, companyId, permissionCode);

            case "project":
                // 1. Kiểm tra quyền trực tiếp trên Project
                boolean hasProjectPerm = projectMemberRepository.checkProjectPermission(userId, targetId, permissionCode);
                if (hasProjectPerm) return true;

                // 2. (Thừa kế) Kiểm tra quyền trên Workspace cha
                Integer workspaceId = getWorkspaceIdFromProject(targetId);
                boolean hasWorkspacePermFromProject = workspaceMemberRepository.checkWorkspacePermission(userId, workspaceId, permissionCode);
                if (hasWorkspacePermFromProject) return true;

                // 3. (Thừa kế) Kiểm tra quyền trên Company cha
                Integer companyIdFromProject = getCompanyIdFromWorkspace(workspaceId);
                return companyMemberRepository.checkCompanyPermission(userId, companyIdFromProject, permissionCode);

            case "task":
                Task task = taskRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Task để kiểm tra quyền."));
                Integer projectId = task.getProject().getId();
                // Gọi lại chính hàm này ở cấp Project
                return hasPermission("project", projectId, permissionCode);

            default:
                throw new IllegalArgumentException("Phạm vi quyền này không xác định: " + scope);
        }
    }
    
    // =============================================
    // KHỐI LOGIC 4: CÁC HÀM KIỂM TRA ROLE CŨ (ĐỂ TƯƠNG THÍCH)
    // (Đã nâng cấp để sử dụng logic Permission-based)
    // =============================================
    
    @Override
    public boolean isCompanyAdmin(Integer companyId) {
        // Admin Cty là người có quyền 'company:manage_roles' (hoặc 'company:edit')
        return hasCompanyPermission(companyId, "company:manage_roles");
    }

    @Override
    public boolean isCompanyMember(Integer companyId) {
        // Member Cty là người có quyền 'company:view' (quyền cơ bản nhất)
        return hasCompanyPermission(companyId, "company:view");
    }

    @Override
    public boolean isWorkspaceAdmin(Integer companyId, Integer workspaceId) {
        // Admin Workspace là người có quyền 'workspace:edit'
        if (!getCompanyIdFromWorkspace(workspaceId).equals(companyId)) {
            return false; // Chống lỗi IDOR
        }
        return hasPermission("workspace", workspaceId, "workspace:edit");
    }

    @Override
    public boolean isWorkspaceMember(Integer companyId, Integer workspaceId) {
        // Member Workspace là người có quyền 'workspace:view'
        if (!getCompanyIdFromWorkspace(workspaceId).equals(companyId)) {
            return false; // Chống lỗi IDOR
        }
        return hasPermission("workspace", workspaceId, "workspace:view");
    }

    @Override
    public boolean canManageWorkspaceMembers(Integer companyId, Integer workspaceId) {
        // Người quản lý là người có quyền 'workspace:invite_member'
        // Hàm hasPermission đã tự xử lý thừa kế (Admin Cty cũng có quyền này)
        return hasPermission("workspace", workspaceId, "workspace:invite_member");
    }

    @Override
    public boolean hasSprintPermission(Integer sprintId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || sprintId == null) return false;
        
        // 1. Tìm Sprint để lấy Project ID
        Sprint sprint = sprintRepository.findById(sprintId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint để kiểm tra quyền."));
        
        Integer projectId = sprint.getProject().getId();
        
        // 2. Gọi kiểm tra quyền của Project
        return hasPermission("project", projectId, permissionCode);
    }
    
}