package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của một lời mời tham gia (Công ty, Dự án, Workspace, v.v.).
 */
public enum InvitationStatus {

    // ==========================================
    // ENUM VALUES (Giá trị trạng thái)
    // ==========================================

    /** Đang chờ phản hồi từ người được mời */
    PENDING,

    /** Người dùng đã đồng ý tham gia */
    ACCEPTED,

    /** Đã quá thời hạn hiệu lực của Token lời mời */
    EXPIRED,

    /** Lời mời đã bị người gửi (Admin/Manager) thu hồi */
    CANCELLED

}