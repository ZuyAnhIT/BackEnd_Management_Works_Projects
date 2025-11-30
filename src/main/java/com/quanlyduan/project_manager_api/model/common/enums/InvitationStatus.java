// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/InvitationStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của một lời mời tham gia (Công ty, Dự án, v.v.).
 */
public enum InvitationStatus {
    PENDING,    // Đang chờ phản hồi
    ACCEPTED,   // Đã chấp nhận lời mời
    EXPIRED,    // Đã hết thời hạn chấp nhận
    CANCELLED   // Đã bị hủy bỏ bởi người gửi
}