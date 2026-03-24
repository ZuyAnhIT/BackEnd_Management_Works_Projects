package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CheckoutRequest;
import com.quanlyduan.project_manager_api.dto.response.CheckoutResponse;


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

}