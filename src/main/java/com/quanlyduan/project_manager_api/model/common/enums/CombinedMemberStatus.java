package com.quanlyduan.project_manager_api.model.common.enums;

public enum CombinedMemberStatus {
    ACTIVE,    // Đã là thành viên và đang "Hoạt động"
    INACTIVE,  // Đã là thành viên nhưng "Tạm dừng" hoặc "Đã rời"
    PENDING    // Chưa là thành viên, lời mời đang "Chờ"
}