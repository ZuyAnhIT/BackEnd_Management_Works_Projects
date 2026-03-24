package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Các trạng thái vòng đời của một giao dịch thanh toán.
 */
public enum TransactionStatus {
    PENDING,    // Đang chờ thanh toán (Khách vừa tạo link QR nhưng chưa quét)
    SUCCESS,    // Thanh toán thành công (Tiền đã vào tài khoản, Webhook đã xác nhận)
    FAILED,     // Thanh toán thất bại (Lỗi từ ngân hàng hoặc cổng thanh toán)
    CANCELLED,  // Khách hàng chủ động hủy giao dịch (Bấm nút Hủy trên trang PayOS)
    REFUNDED    // Đã hoàn tiền (Admin chủ động hoàn tiền cho khách)
}