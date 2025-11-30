// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/CompanyStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái hoạt động của Công ty.
 */
public enum CompanyStatus {
    ACTIVE,     // 'Hoạt động' bình thường
    SUSPENDED,  // 'Bị tạm dừng' (Ví dụ: hết hạn hợp đồng, vi phạm quy định)
    DELETED     // 'Đã xóa' (Xóa mềm - Soft Delete)
}