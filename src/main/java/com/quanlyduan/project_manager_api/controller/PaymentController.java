package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CancelPaymentRequest;
import com.quanlyduan.project_manager_api.dto.request.CheckoutRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.CheckoutResponse;
import com.quanlyduan.project_manager_api.security.UserPrincipal;
import com.quanlyduan.project_manager_api.service.PaymentService;

// 👉 Import chuẩn của PayOS v2.0.1
import vn.payos.model.webhooks.Webhook;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * API 1: Tạo Link Thanh Toán (Checkout)
     */
    @PostMapping("/checkout")
    @PreAuthorize("@securityService.hasCompanyPermission(#request.companyId, 'company:manage_billing')")
    public ResponseEntity<ApiResponse<CheckoutResponse>> createCheckoutLink(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Integer currentUserId = currentUser.getId();

        CheckoutResponse response = paymentService.createPaymentLink(request.getCompanyId(), currentUserId, request);

        return ResponseEntity.ok(ApiResponse.success("Tạo link thanh toán thành công.", response));
    }

    @PostMapping("/webhook")
    // Đổi tham số thành String nguyên thủy để KHÔNG BAO GIỜ bị lỗi ép kiểu
    public ResponseEntity<Map<String, Object>> handlePayOSWebhook(@RequestBody String rawBody) {
        
        // Dùng System.out.println thay vì log.info để ép buộc in ra màn hình, bất chấp cấu hình log
        System.out.println("==================================================");
        System.out.println("[TÍN HIỆU TỪ PAYOS] ĐÃ CHẠM VÀO BACKEND!");
        System.out.println("📦 Dữ liệu thô: " + rawBody);
        System.out.println("==================================================");

        Map<String, Object> response = new HashMap<>();
        
        try {
            // Tự tay ép kiểu từ String sang Object Webhook
            ObjectMapper mapper = new ObjectMapper();
            Webhook webhookBody = mapper.readValue(rawBody, Webhook.class);
            
            // Chuyển xuống Service xử lý
            paymentService.processWebhook(webhookBody);
            
            response.put("success", true);
            response.put("message", "Webhook processed successfully.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("CÓ LỖI XẢY RA KHI XỬ LÝ WEBHOOK:");
            e.printStackTrace(); 
            
            response.put("success", true); 
            response.put("message", "Error but ignored");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * API 2: Khách hàng chủ động hủy đơn hàng (Cancel PENDING Transaction)
     */
    @PostMapping("/checkout/{transactionCode}/cancel")
    @PreAuthorize("@securityService.hasCompanyPermission(#request.companyId, 'company:manage_billing')")
    public ResponseEntity<ApiResponse<String>> cancelPaymentLink(
            @PathVariable String transactionCode,
            @Valid @RequestBody CancelPaymentRequest request) {

        paymentService.cancelPendingTransaction(transactionCode, request.getCompanyId(), request.getCancellationReason());

        return ResponseEntity.ok(ApiResponse.success("Hủy giao dịch thành công.", null));
    }
}