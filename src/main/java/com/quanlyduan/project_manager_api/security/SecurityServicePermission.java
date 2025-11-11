// File: src/main/java/com/quanlyduan/project_manager_api/security/SecurityService.java
// *** HOÀN TOÀN MỚI ***
package com.quanlyduan.project_manager_api.security;

public interface SecurityServicePermission {

    // Lấy thông tin User
    String getCurrentUserEmail();
    Integer getCurrentUserId();

    // === HÀM KIỂM TRA QUYỀN HẠN MỚI ===

    /**
     * Kiểm tra user có quyền <permissionCode> tại công ty <companyId> không.
     * @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:edit')")
     */
    boolean hasCompanyPermission(Integer companyId, String permissionCode);

    /**
     * Kiểm tra user có quyền <permissionCode> tại workspace <workspaceId> không.
     * @PreAuthorize("@securityService.hasWorkspacePermission(#workspaceId, 'project:create')")
     */
    boolean hasWorkspacePermission(Integer workspaceId, String permissionCode);

    /**
     * Kiểm tra user có quyền <permissionCode> tại project <projectId> không.
     * @PreAuthorize("@securityService.hasProjectPermission(#projectId, 'task:create')")
     */
    boolean hasProjectPermission(Integer projectId, String permissionCode);

    /**
     * Kiểm tra user có quyền <permissionCode> liên quan đến task <taskId> không.
     * (Hàm này sẽ tìm projectId từ taskId rồi gọi hasProjectPermission)
     * @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:comment')")
     */
    boolean hasTaskPermission(Integer taskId, String permissionCode);
    boolean hasPermission(String scope, Integer targetId, String permissionCode);
}
