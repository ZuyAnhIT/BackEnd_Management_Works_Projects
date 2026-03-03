package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Enum định nghĩa các trạng thái của Gói cước (Subscription) mà Công ty đang sử dụng.
 * Áp dụng trong hệ thống quản lý thuê bao SaaS (Software as a Service).
 */
public enum SubscriptionStatus {

    /**
     * Khách hàng đang trong thời gian dùng thử miễn phí (Trial).
     * Thường áp dụng ngay khi mới tạo Công ty.
     */
    TRIAL,

    /**
     * Gói cước đang hoạt động bình thường.
     * Áp dụng cho cả gói Miễn phí (FREE) trọn đời hoặc gói Trả phí (PRO/ENTERPRISE) đã thanh toán.
     */
    ACTIVE,

    /**
     * Khách hàng đã quá hạn thanh toán chu kỳ tiếp theo nhưng hệ thống vẫn đang cho 
     * một khoảng thời gian ân hạn (Grace Period) trước khi cắt dịch vụ.
     */
    PAST_DUE,

    /**
     * Khách hàng chủ động hủy gói cước. 
     * Hệ thống sẽ không tự động gia hạn vào kỳ tiếp theo, nhưng khách hàng vẫn có thể 
     * tiếp tục sử dụng cho đến hết ngày của kỳ hiện tại.
     */
    CANCELED,

    /**
     * Gói cước đã hoàn toàn hết hạn (Hết hạn dùng thử hoặc Hết hạn gói trả phí mà không gia hạn).
     * Lúc này, tài nguyên của Công ty (Workspace, Project) có thể bị khóa hoặc chuyển về chế độ Read-only.
     */
    EXPIRED

}