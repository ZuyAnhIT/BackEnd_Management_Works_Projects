package com.quanlyduan.project_manager_api.service;

import java.time.LocalDateTime;

import com.quanlyduan.project_manager_api.dto.request.CheckoutRequest;
import com.quanlyduan.project_manager_api.dto.response.CheckoutResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.TransactionDetailResponse;
import com.quanlyduan.project_manager_api.dto.response.TransactionListResponse;

import vn.payos.model.webhooks.Webhook;

/**
 * Service xử lý các nghiệp vụ liên quan đến Thanh toán và Cổng thanh toán (PayOS).
 */
public interface PaymentService {

    /**
     * Tạo link thanh toán (Checkout URL) chứa mã QR Code để khách hàng quét.
     */
    CheckoutResponse createPaymentLink(Integer companyId, Integer userId, CheckoutRequest request);

    /**
     * Hàm xử lý Webhook khi PayOS gọi về thông báo giao dịch thành công.
     */
    void processWebhook(Webhook webhookBody);

    void cancelPendingTransaction(String transactionCode, Integer companyId, String reason);

    // 1. Hàm lấy danh sách giao dịch (Có phân trang và bộ lọc)
    PageResponseDTO<TransactionListResponse> getTransactionHistory(
            Integer companyId, 
            int page, 
            int size, 
            String status, 
            LocalDateTime startDate, 
            LocalDateTime endDate
    );

    // 2. Hàm xem chi tiết 1 giao dịch cụ thể
    TransactionDetailResponse getTransactionDetail(
            String transactionCode, 
            Integer companyId
    );
}