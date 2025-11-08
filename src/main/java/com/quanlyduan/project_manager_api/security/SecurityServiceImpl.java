// File: src/main/java/com/quanlyduan/project_manager_api/security/SecurityServiceImpl.java
// *** HOÀN TOÀN MỚI ***

package com.quanlyduan.project_manager_api.security;

import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
// import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
// import com.quanlyduan.project_manager_api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("securityServicePermission") // Đặt tên Bean là "securityService" để @PreAuthorize có thể tìm thấy
@RequiredArgsConstructor
@Transactional(readOnly = true) // Các hàm kiểm tra quyền chỉ đọc
public class SecurityServiceImpl implements SecurityServicePermission {

    private final RoleRepository roleRepository;
    // private final TaskRepository taskRepository;

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
    public boolean hasProjectPermission(Integer projectId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || projectId == null)
            return false;

        // Gọi truy vấn JPA đã định nghĩa
        return roleRepository.checkProjectPermission(userId, projectId, permissionCode);
    }

    // @Override
    // public boolean hasTaskPermission(Integer taskId, String permissionCode) {
    // Integer userId = getCurrentUserId();
    // if (userId == null || taskId == null) return false;

    // // 1. Tìm Task để lấy Project ID
    // Task task = taskRepository.findById(taskId)
    // .orElseThrow(() -> new ResourceNotFoundException("Task not found for
    // permission check"));

    // Integer projectId = task.getProject().getId();

    // // 2. Gọi kiểm tra quyền của Project
    // return roleRepository.checkProjectPermission(userId, projectId,
    // permissionCode);
    // }
}
