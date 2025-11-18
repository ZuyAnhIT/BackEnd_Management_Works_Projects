// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/CombinedMemberStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

public enum CombinedMemberStatus {
    ACTIVE,    // Đã là thành viên và đang "Hoạt động"
   SUSPENDED, // Đã là thành viên nhưng bị "Tạm dừng"
    REMOVED,   // Đã là thành viên nhưng bị "Xóa/Rời khỏi"
    PENDING    // Chưa là thành viên, lời mời đang "Chờ"
}