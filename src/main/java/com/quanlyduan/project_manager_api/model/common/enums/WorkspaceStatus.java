package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của một Workspace (Không gian làm việc).
 * Tương ứng với ENUM('Hoạt động', 'Lưu trữ', 'Đã xóa') trong cấu hình bảng workspaces.
 */
public enum WorkspaceStatus {

    // ==========================================
    // ENUM VALUES (Giá trị trạng thái Workspace)
    // ==========================================

    /** 'Hoạt động' - Workspace đang được sử dụng và có thể thao tác bình thường */
    ACTIVE,

    /** * 'Lưu trữ' - Workspace không còn hoạt động tích cực.
     * Dữ liệu vẫn được giữ lại để tra cứu nhưng bị hạn chế quyền chỉnh sửa.
     */
    ARCHIVED,

    /** * 'Đã xóa' - Workspace không còn tồn tại trong logic nghiệp vụ (Soft Delete).
     * Dữ liệu vẫn được lưu trong DB để đảm bảo tính toàn vẹn của hệ thống.
     */
    DELETED

}