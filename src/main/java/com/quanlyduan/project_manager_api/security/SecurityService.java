// File: src/main/java/com.quanlyduan.project_manager_api/security/SecurityService.java
package com.quanlyduan.project_manager_api.security;

import com.quanlyduan.project_manager_api.model.User;

public interface SecurityService {

    // ========================================================================
    // 1. CÁC HÀM LẤY THÔNG TIN USER (IDENTITY)
    // ========================================================================

    /**
     * Lấy email của user đang đăng nhập (từ UserPrincipal).
     * @return Email của người dùng, hoặc null nếu không có user xác thực.
     */
    String getCurrentUserEmail();

    /**
     * Lấy ID của user đang đăng nhập (từ UserPrincipal).
     * @return ID của người dùng, hoặc null nếu không có user xác thực.
     */
    Integer getCurrentUserId();

    /**
     * Lấy toàn bộ Entity User (NguoiDung) từ CSDL.
     * Dùng cho các service cần đối tượng User đầy đủ.
     * @return Đối tượng User đã xác thực.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException Nếu user không tồn tại hoặc không được xác thực.
     */
    User getCurrentAuthenticatedUser();

    // ========================================================================
    // 2. HÀM KIỂM TRA QUYỀN HẠN (PERMISSION-BASED CHECKERS)
    // ========================================================================

    /**
     * Kiểm tra user có quyền <permissionCode> ở cấp độ HỆ THỐNG (SYSTEM) không.
     * @param permissionCode Mã quyền (ví dụ: 'company:create').
     * @PreAuthorize("@securityService.hasSystemPermission('company:create')")
     */
    boolean hasSystemPermission(String permissionCode);
    
    /**
     * Kiểm tra user có quyền <permissionCode> tại công ty <companyId> không.
     * @param companyId ID công ty.
     * @param permissionCode Mã quyền (ví dụ: 'company:edit').
     * @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:edit')")
     */
    boolean hasCompanyPermission(Integer companyId, String permissionCode);

    /**
     * Kiểm tra user có quyền <permissionCode> tại workspace <workspaceId> không.
     * @param workspaceId ID không gian làm việc.
     * @param permissionCode Mã quyền (ví dụ: 'project:create').
     * @PreAuthorize("@securityService.hasWorkspacePermission(#workspaceId, 'project:create')")
     */
    boolean hasWorkspacePermission(Integer workspaceId, String permissionCode);

    /**
     * Kiểm tra user có quyền <permissionCode> tại project <projectId> không.
     * @param projectId ID dự án.
     * @param permissionCode Mã quyền (ví dụ: 'task:create').
     * @PreAuthorize("@securityService.hasProjectPermission(#projectId, 'task:create')")
     */
    boolean hasProjectPermission(Integer projectId, String permissionCode);
    
    /**
     * Kiểm tra user có quyền <permissionCode> liên quan đến sprint <sprintId> không.
     * (Hàm này sẽ tìm projectId từ sprintId rồi gọi hasProjectPermission)
     * @param sprintId ID Sprint.
     * @param permissionCode Mã quyền (ví dụ: 'project:view').
     * @PreAuthorize("@securityService.hasSprintPermission(#sprintId, 'project:view')")
     */
    boolean hasSprintPermission(Integer sprintId, String permissionCode);

    /**
     * Kiểm tra user có quyền <permissionCode> liên quan đến task <taskId> không.
     * (Hàm này sẽ tìm projectId từ taskId rồi gọi hasProjectPermission)
     * @param taskId ID Task.
     * @param permissionCode Mã quyền (ví dụ: 'task:comment').
     * @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:comment')")
     */
    boolean hasTaskPermission(Integer taskId, String permissionCode);
    
    /**
     * Hàm kiểm tra quyền thừa kế (Hierarchy Permission Check).
     * Kiểm tra quyền <permissionCode> tại phạm vi <scope> (project, workspace, company)
     * bao gồm cả việc thừa kế từ cấp cao hơn (ví dụ: Admin Công ty có quyền trên Project).
     * @param scope Phạm vi (Ví dụ: 'project', 'workspace').
     * @param targetId ID của phạm vi đó.
     * @param permissionCode Mã quyền.
     * @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
     */
    boolean hasPermission(String scope, Integer targetId, String permissionCode);


    // ========================================================================
    // 3. CÁC HÀM TIỆN ÍCH (LEGACY/UTILITY ROLE CHECKERS)
    // ========================================================================

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
     * [ĐÃ NÂNG CẤP] Kiểm tra user có quyền quản lý Workspace Member không (thêm/xóa/đổi role).
     */
    boolean canManageWorkspaceMembers(Integer companyId, Integer workspaceId);
}