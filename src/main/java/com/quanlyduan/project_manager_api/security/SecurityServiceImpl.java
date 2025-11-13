// File: src/main/java/com/quanlyduan/project_manager_api/security/SecurityServiceImpl.java
// *** HOÀN TOÀN MỚI ***

package com.quanlyduan.project_manager_api.security;

import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
// import com.quanlyduan.project_manager_api.model.Task;
import java.util.List;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRoleRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("securityServicePermission") // Đặt tên Bean là "securityServicePermission" để @PreAuthorize có thể tìm thấy
@RequiredArgsConstructor
@Transactional(readOnly = true) // Các hàm kiểm tra quyền chỉ đọc
public class SecurityServiceImpl implements SecurityServicePermission {

    private final CompanyMemberRepository companyMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRoleRepository userRoleRepository;

    private final RoleRepository roleRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;

    private UserPrincipal getCurrentUser() {
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
        UserPrincipal user = getCurrentUser();
        return (user != null) ? user.getEmail() : null;
    }

    @Override
    public Integer getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return (user != null) ? user.getId() : null;
    }
    private Integer getCompanyIdFromWorkspace(Integer workspaceId) {
    // Truy vấn workspace để lấy companyId
    return workspaceRepository.findById(workspaceId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Workspace."))
        .getCompany().getId();
}

private Integer getWorkspaceIdFromProject(Integer projectId) {
    return projectRepository.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Project."))
        .getWorkspace().getId();
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
    public boolean hasSystemPermission(String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null)
            return false;
        // Gọi repo chính xác
        return userRoleRepository.checkSystemPermission(userId, permissionCode);
    }

    @Override
    public boolean hasTaskPermission(Integer taskId, String permissionCode) {
        // Hàm này không đổi, logic vẫn đúng
        Integer userId = getCurrentUserId();
        if (userId == null || taskId == null) return false;
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("hông tìm thấy Task để kiểm tra quyền."));
        Integer projectId = task.getProject().getId();
        return hasPermission("project", projectId, permissionCode);
    }

    @Override
    public boolean hasPermission(String scope, Integer targetId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || targetId == null || scope == null || permissionCode == null)
            return false;

        switch (scope.toLowerCase()) {
            case "company":
                // Sửa: Gọi repo chính xác
                return companyMemberRepository.checkCompanyPermission(userId, targetId, permissionCode);

            case "workspace":
                // Sửa: Gọi repo chính xác
                boolean hasWorkspace = workspaceMemberRepository.checkWorkspacePermission(userId, targetId, permissionCode);
                if (hasWorkspace) return true;

                Integer companyId = getCompanyIdFromWorkspace(targetId);
                // Sửa: Gọi repo chính xác
                return companyMemberRepository.checkCompanyPermission(userId, companyId, permissionCode);

            case "project":
                // Sửa: Gọi repo chính xác
                boolean hasProject = projectMemberRepository.checkProjectPermission(userId, targetId, permissionCode);
                if (hasProject) return true;

                Integer workspaceId = getWorkspaceIdFromProject(targetId);
                // Sửa: Gọi repo chính xác
                boolean hasWorkspaceFromProject = workspaceMemberRepository.checkWorkspacePermission(userId, workspaceId, permissionCode);
                if (hasWorkspaceFromProject) return true;

                Integer companyIdFromProject = getCompanyIdFromWorkspace(workspaceId);
                // Sửa: Gọi repo chính xác
                return companyMemberRepository.checkCompanyPermission(userId, companyIdFromProject, permissionCode);

            case "task":
                Task task = taskRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Task để kiểm tra quyền."));
                Integer projectId = task.getProject().getId();
                return hasPermission("project", projectId, permissionCode);

            default:
                throw new IllegalArgumentException("Phạm vi quyền này không xác định: " + scope);
        }
    }
}