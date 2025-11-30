// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/CombinedMemberStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái thành viên tổng hợp (dùng cho mục đích hiển thị/DTO).
 * Nó kết hợp cả trạng thái thành viên (ACTIVE, SUSPENDED, REMOVED)
 * và trạng thái lời mời (PENDING).
 */
public enum CombinedMemberStatus {
    ACTIVE,     // Là thành viên và đang "Hoạt động"
    SUSPENDED,  // Là thành viên nhưng bị "Tạm dừng"
    REMOVED,    // Là thành viên nhưng bị "Xóa/Rời khỏi"
    PENDING     // Chưa là thành viên, lời mời đang "Chờ"
}