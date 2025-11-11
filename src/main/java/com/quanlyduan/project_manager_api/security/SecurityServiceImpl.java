// File: src/main/java/com/quanlyduan/project_manager_api/security/SecurityServiceImpl.java
// *** HOÀN TOÀN MỚI ***

package com.quanlyduan.project_manager_api.security;

import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
// import com.quanlyduan.project_manager_api.model.Task;
import java.util.List;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
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
        .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"))
        .getCompany().getId();
}

private Integer getWorkspaceIdFromProject(Integer projectId) {
    return projectRepository.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"))
        .getWorkspace().getId();
}

    @Override
    public boolean hasCompanyPermission(Integer companyId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || companyId == null)
            return false;

        // Gọi truy vấn JPA đã định nghĩa
        return roleRepository.checkCompanyPermission(userId, companyId, permissionCode);
    }

    @Override
    public boolean hasWorkspacePermission(Integer workspaceId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || workspaceId == null)
            return false;

        // Gọi truy vấn JPA đã định nghĩa
        return roleRepository.checkWorkspacePermission(userId, workspaceId, permissionCode);
    }
    @Override
public boolean hasTaskPermission(Integer taskId, String permissionCode) {
    Integer userId = getCurrentUserId();
    if (userId == null || taskId == null) return false;

    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException("Task not found for permission check"));

    Integer projectId = task.getProject().getId();

    // Gọi kiểm tra quyền của Project
    return hasPermission("project", projectId, permissionCode);
}
    @Override
    public boolean hasProjectPermission(Integer projectId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || projectId == null)
            return false;

        // Gọi truy vấn JPA đã định nghĩa
        return roleRepository.checkProjectPermission(userId, projectId, permissionCode);
    }
    


     @Override
public boolean hasPermission(String scope, Integer targetId, String permissionCode) {
    Integer userId = getCurrentUserId();
    if (userId == null || targetId == null || scope == null || permissionCode == null)
        return false;

    switch (scope.toLowerCase()) {
        case "company":
            // Kiểm tra trực tiếp quyền ở cấp company
            boolean hasCompany = roleRepository.checkCompanyPermission(userId, targetId, permissionCode);
            if (hasCompany) return true;

            // Nếu không có, kiểm tra các workspace thuộc company
            List<Integer> workspaceIds = workspaceRepository.findWorkspaceIdsByCompanyId(targetId);
            for (Integer wid : workspaceIds) {
                if (roleRepository.checkWorkspacePermission(userId, wid, permissionCode)) return true;

                // Nếu không có ở workspace, kiểm tra các project thuộc workspace
                List<Integer> projectIds = projectRepository.findProjectIdsByWorkspaceId(wid);
                for (Integer pid : projectIds) {
                    if (roleRepository.checkProjectPermission(userId, pid, permissionCode)) return true;
                }
            }
            return false;

        case "workspace":
            boolean hasWorkspace = roleRepository.checkWorkspacePermission(userId, targetId, permissionCode);
            if (hasWorkspace) return true;

            Integer companyId = getCompanyIdFromWorkspace(targetId);
            return roleRepository.checkCompanyPermission(userId, companyId, permissionCode);

        case "project":
            boolean hasProject = roleRepository.checkProjectPermission(userId, targetId, permissionCode);
            if (hasProject) return true;

            Integer workspaceId = getWorkspaceIdFromProject(targetId);
            boolean hasWorkspaceFromProject = roleRepository.checkWorkspacePermission(userId, workspaceId, permissionCode);
            if (hasWorkspaceFromProject) return true;

            Integer companyIdFromProject = getCompanyIdFromWorkspace(workspaceId);
            return roleRepository.checkCompanyPermission(userId, companyIdFromProject, permissionCode);

        case "task":
            Task task = taskRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found for permission check"));
            Integer projectId = task.getProject().getId();
            return hasPermission("project", projectId, permissionCode);

        default:
            throw new IllegalArgumentException("Unknown permission scope: " + scope);
    }
}
    
}
