// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/MemberStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái thành viên trong một mối quan hệ (Công ty, Workspace, Dự án).
 */
public enum MemberStatus {
    ACTIVE,     // Hoạt động bình thường
    SUSPENDED,  // Bị tạm dừng (ví dụ: đang nghỉ phép, không được truy cập)
    REMOVED     // Đã bị xóa hoặc tự rời khỏi
}