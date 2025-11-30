// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/WorkspaceStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của một Workspace (Không gian làm việc).
 * Dựa trên ENUM('Hoạt động', 'Lưu trữ', 'Đã xóa') của bảng KhongGian.
 */
public enum WorkspaceStatus {
    ACTIVE,     // 'Hoạt động' (Workspace đang được sử dụng bình thường)
    ARCHIVED,   // 'Lưu trữ' (Workspace không còn được sử dụng tích cực nhưng vẫn giữ lại dữ liệu)
    DELETED     // 'Đã xóa' (Xóa mềm - Soft Delete)
}