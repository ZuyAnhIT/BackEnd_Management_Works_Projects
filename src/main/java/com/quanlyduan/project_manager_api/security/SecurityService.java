// File: src/main/java/com/quanlyduan/project_manager_api/security/SecurityService.java
package com.quanlyduan.project_manager_api.security;

import com.quanlyduan.project_manager_api.model.User;

public interface SecurityService {

    // === CÁC HÀM LẤY THÔNG TIN USER ===
    /**
     * Lấy email của user đang đăng nhập (từ UserPrincipal).
     * Trả về null nếu không ai đăng nhập.
     */
    String getCurrentUserEmail();

    /**
     * Lấy ID của user đang đăng nhập (từ UserPrincipal).
     * Trả về null nếu không ai đăng nhập.
     */
    Integer getCurrentUserId();

    /**
     * Lấy toàn bộ Entity User (NguoiDung) từ CSDL.
     * Dùng cho các service cần đối tượng User đầy đủ.
     * Ném lỗi nếu không tìm thấy user.
     */
    User getCurrentAuthenticatedUser();

    // === HÀM KIỂM TRA QUYỀN HẠN MỚI (PERMISSION-BASED) ===

    /**
     * Kiểm tra user có quyền <permissionCode> ở cấp độ HỆ THỐNG không.
     * @PreAuthorize("@securityService.hasSystemPermission('company:create')")
     */
    boolean hasSystemPermission(String permissionCode);
    
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
    
    /**
     * Hàm kiểm tra quyền thừa kế (VD: Admin Cty có quyền trên Project)
     * @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
     */
    boolean hasPermission(String scope, Integer targetId, String permissionCode);


    // === CÁC HÀM TIỆN ÍCH KIỂM TRA ROLE CŨ (ĐỂ TƯƠNG THÍCH) ===
    // (Những hàm này giờ sẽ gọi hàm hasPermission bên trên)

    /**
     * [ĐÃ NÂNG CẤP] Kiểm tra user có phải là Company Admin không.
     */
    boolean isCompanyAdmin(Integer companyId);

    /**
     * [ĐÃ NÂNG CẤP] Kiểm tra user có phải là Company Member không.
     */
    boolean isCompanyMember(Integer companyId);

    /**
     * [ĐÃ NÂNG CẤP] Kiểm tra user có phải là Workspace Admin không.
     */
    boolean isWorkspaceAdmin(Integer companyId, Integer workspaceId);

    /**
     * [ĐÃ NÂNG CẤP] Kiểm tra user có phải là Workspace Member không.
     */
    boolean isWorkspaceMember(Integer companyId, Integer workspaceId);

    /**
     * [ĐÃ NÂNG CẤP] Kiểm tra user có quyền quản lý Workspace Member không.
     */
    boolean canManageWorkspaceMembers(Integer companyId, Integer workspaceId);

    /**
     * Kiểm tra user có quyền <permissionCode> liên quan đến sprint <sprintId> không.
     * (Hàm này sẽ tìm projectId từ sprintId rồi gọi hasProjectPermission)
     * @PreAuthorize("@securityService.hasSprintPermission(#sprintId, 'project:view')")
     */
    boolean hasSprintPermission(Integer sprintId, String permissionCode);
}