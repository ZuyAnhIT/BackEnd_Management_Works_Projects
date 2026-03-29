package com.quanlyduan.project_manager_api.security;

import com.quanlyduan.project_manager_api.model.User;

/**
 * Interface định nghĩa các dịch vụ bảo mật và kiểm tra quyền hạn (Security & Authorization).
 * Cung cấp các phương thức để xác thực danh tính người dùng và kiểm tra quyền truy cập 
 * theo mô hình phân cấp (Hierarchy-based Access Control).
 */
public interface SecurityService {

    // ========================================================================
    // XÁC THỰC & ĐỊNH DANH (IDENTITY)
    // ========================================================================

    /**
     * Lấy địa chỉ Email của người dùng đang đăng nhập từ Security Context.
     * @return Email của người dùng, hoặc null nếu chưa xác thực.
     */
    String getCurrentUserEmail();

    /**
     * Lấy ID của người dùng đang đăng nhập từ Security Context.
     * @return ID người dùng, hoặc null nếu chưa xác thực.
     */
    Integer getCurrentUserId();

    /**
     * Truy xuất toàn bộ thực thể User từ cơ sở dữ liệu dựa trên danh tính đã xác thực.
     * @return Đối tượng User đầy đủ.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException nếu không tìm thấy người dùng.
     */
    User getCurrentAuthenticatedUser();

    // ========================================================================
    // CƠ CHẾ KIỂM TRA QUYỀN TỔNG QUÁT (CORE PERMISSION ENGINE)
    // ========================================================================

    /**
     * Kiểm tra quyền hạn linh hoạt dựa trên phạm vi (Scope) và phân cấp thừa kế.
     * Ví dụ: Admin Công ty tự động có quyền trên các Project thuộc Công ty đó.
     * * @param scope Phạm vi cần kiểm tra ('project', 'workspace', 'company').
     * @param targetId ID của đối tượng mục tiêu.
     * @param permissionCode Mã quyền cần kiểm tra (ví dụ: 'project:edit').
     * @example @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
     */
    boolean hasPermission(String scope, Integer targetId, String permissionCode);

    // ========================================================================
    // KIỂM TRA QUYỀN THEO TÀI NGUYÊN (RESOURCE PERMISSIONS)
    // ========================================================================

    /**
     * Kiểm tra quyền hạn ở cấp độ quản trị hệ thống (System Level).
     */
    boolean hasSystemPermission(String permissionCode);
    
    /**
     * Kiểm tra quyền hạn tại cấp độ Công ty.
     * @example @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:edit')")
     */
    boolean hasCompanyPermission(Integer companyId, String permissionCode);

    /**
     * Kiểm tra quyền hạn tại cấp độ Không gian làm việc (Workspace).
     * @example @PreAuthorize("@securityService.hasWorkspacePermission(#workspaceId, 'project:create')")
     */
    boolean hasWorkspacePermission(Integer workspaceId, String permissionCode);

    /**
     * Kiểm tra quyền hạn trực tiếp tại cấp độ Dự án.
     * @example @PreAuthorize("@securityService.hasProjectPermission(#projectId, 'task:create')")
     */
    boolean hasProjectPermission(Integer projectId, String permissionCode);
    
    /**
     * Kiểm tra quyền hạn dự án dựa trên ID của Sprint.
     * @example @PreAuthorize("@securityService.hasSprintPermission(#sprintId, 'project:view')")
     */
    boolean hasSprintPermission(Integer sprintId, String permissionCode);

    /**
     * Kiểm tra quyền hạn dự án hoặc quyền riêng biệt dựa trên ID của Công việc (Task).
     * @example @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:comment')")
     */
    boolean hasTaskPermission(Integer taskId, String permissionCode);

    // ========================================================================
    // KIỂM TRA VAI TRÒ & TIỆN ÍCH (ROLES & UTILITIES)
    // ========================================================================

    /**
     * Kiểm tra người dùng có phải là Quản trị viên của công ty hay không.
     */
    boolean isCompanyAdmin(Integer companyId);

    /**
     * Kiểm tra người dùng có phải là thành viên chính thức của công ty hay không.
     */
    boolean isCompanyMember(Integer companyId);

    /**
     * Kiểm tra người dùng có vai trò Quản trị viên trong không gian làm việc hay không.
     */
    boolean isWorkspaceAdmin(Integer companyId, Integer workspaceId);

    /**
     * Kiểm tra người dùng có tham gia vào không gian làm việc hay không.
     */
    boolean isWorkspaceMember(Integer companyId, Integer workspaceId);

    /**
     * Kiểm tra người dùng có quyền thực hiện các thao tác quản lý thành viên (thêm/xóa/đổi role) trong Workspace.
     */
    boolean canManageWorkspaceMembers(Integer companyId, Integer workspaceId);
}