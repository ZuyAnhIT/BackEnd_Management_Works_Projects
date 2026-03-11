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

@Service("securityService") // Đặt tên Bean là "securityService"
@Transactional(readOnly = true) // Các hàm kiểm tra quyền chỉ đọc
public class SecurityServiceImpl implements SecurityService {

    // === TẤT CẢ REPOSITORIES CẦN THIẾT ===
    private final CompanyMemberRepository companyMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;

    // === CONSTRUCTOR THỦ CÔNG ===
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

    // ========================================================================
    // KHỐI 1: LẤY THÔNG TIN NGƯỜI DÙNG HIỆN TẠI (IDENTITY)
    // ========================================================================

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal)) {
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
    
    @Override
    public User getCurrentAuthenticatedUser() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            throw new UsernameNotFoundException("Authenticated user information not found."); 
        }
        return userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
    }

    // ========================================================================
    // KHỐI 2: HÀM TIỆN ÍCH TRUY VẤN NGƯỢC (HIERARCHY HELPERS)
    // ========================================================================

    private Integer getCompanyIdFromWorkspace(Integer workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."))
                .getCompany().getId();
    }

    private Integer getWorkspaceIdFromProject(Integer projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."))
                .getWorkspace().getId();
    }

    // ========================================================================
    // KHỐI 3: KIỂM TRA QUYỀN HẠN (PERMISSION-BASED)
    // ========================================================================

    @Override
    public boolean hasSystemPermission(String permissionCode) {
        // 1. Lấy ID người dùng đang đăng nhập từ Context
        Integer currentUserId = getCurrentUserId();
        
        // Bắt lỗi an toàn nếu chưa đăng nhập
        if (currentUserId == null) {
            return false; 
        }

        // 2. Gọi xuống Database để kiểm tra (Trả về > 0 nghĩa là có quyền)
        int permissionCount = userRepository.countSystemPermission(currentUserId, permissionCode);
        
        return permissionCount > 0;
    }
    
    @Override
    public boolean hasCompanyPermission(Integer companyId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || companyId == null)
            return false;
        return companyMemberRepository.checkCompanyPermission(userId, companyId, permissionCode);
    }

    @Override
    public boolean hasWorkspacePermission(Integer workspaceId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || workspaceId == null)
            return false;
        return workspaceMemberRepository.checkWorkspacePermission(userId, workspaceId, permissionCode);
    }
    
    @Override
    public boolean hasProjectPermission(Integer projectId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || projectId == null)
            return false;
        return projectMemberRepository.checkProjectPermission(userId, projectId, permissionCode);
    }
    
    @Override
    public boolean hasTaskPermission(Integer taskId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || taskId == null) return false;
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found for permission check."));
        
        Integer projectId = task.getProject().getId();
        return hasPermission("project", projectId, permissionCode);
    }

    @Override
    public boolean hasSprintPermission(Integer sprintId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || sprintId == null) return false;
        
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found for permission check."));
        
        Integer projectId = sprint.getProject().getId();
        return hasPermission("project", projectId, permissionCode);
    }

    @Override
    public boolean hasPermission(String scope, Integer targetId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || targetId == null || scope == null || permissionCode == null)
            return false;

        switch (scope.toLowerCase()) {
            case "company":
                return companyMemberRepository.checkCompanyPermission(userId, targetId, permissionCode);

            case "workspace":
                boolean hasWorkspacePerm = workspaceMemberRepository.checkWorkspacePermission(userId, targetId, permissionCode);
                if (hasWorkspacePerm) return true;

                Integer companyId = getCompanyIdFromWorkspace(targetId);
                return companyMemberRepository.checkCompanyPermission(userId, companyId, permissionCode);

            case "project":
                boolean hasProjectPerm = projectMemberRepository.checkProjectPermission(userId, targetId, permissionCode);
                if (hasProjectPerm) return true;

                Integer workspaceId = getWorkspaceIdFromProject(targetId);
                boolean hasWorkspacePermFromProject = workspaceMemberRepository.checkWorkspacePermission(userId, workspaceId, permissionCode);
                if (hasWorkspacePermFromProject) return true;

                Integer companyIdFromProject = getCompanyIdFromWorkspace(workspaceId);
                return companyMemberRepository.checkCompanyPermission(userId, companyIdFromProject, permissionCode);

            case "task":
                Task task = taskRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Task not found for permission check."));
                Integer projectId = task.getProject().getId();
                return hasPermission("project", projectId, permissionCode);
            
            case "sprint":
                Sprint sprint = sprintRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Sprint not found for permission check."));
                Integer projectIdS = sprint.getProject().getId();
                return hasPermission("project", projectIdS, permissionCode);

            default:
                throw new IllegalArgumentException("Unknown permission scope: " + scope);
        }
    }
    
    // ========================================================================
    // KHỐI 4: CÁC HÀM TIỆN ÍCH KIỂM TRA ROLE CŨ (COMPATIBILITY)
    // ========================================================================
    
    @Override
    public boolean isCompanyAdmin(Integer companyId) {
        return hasCompanyPermission(companyId, "company:manage_roles");
    }

    @Override
    public boolean isCompanyMember(Integer companyId) {
        return hasCompanyPermission(companyId, "company:view");
    }

    @Override
    public boolean isWorkspaceAdmin(Integer companyId, Integer workspaceId) {
        if (!getCompanyIdFromWorkspace(workspaceId).equals(companyId)) {
            return false;
        }
        return hasPermission("workspace", workspaceId, "workspace:edit");
    }

    @Override
    public boolean isWorkspaceMember(Integer companyId, Integer workspaceId) {
        if (!getCompanyIdFromWorkspace(workspaceId).equals(companyId)) {
            return false;
        }
        return hasPermission("workspace", workspaceId, "workspace:view");
    }

    @Override
    public boolean canManageWorkspaceMembers(Integer companyId, Integer workspaceId) {
        return hasPermission("workspace", workspaceId, "workspace:invite_member");
    }
}