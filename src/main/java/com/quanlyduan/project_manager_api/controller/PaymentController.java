package com.quanlyduan.project_manager_api.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.dto.request.CancelPaymentRequest;
import com.quanlyduan.project_manager_api.dto.request.CheckoutRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.CheckoutResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.TransactionDetailResponse;
import com.quanlyduan.project_manager_api.dto.response.TransactionListResponse;
import com.quanlyduan.project_manager_api.security.UserPrincipal;
import com.quanlyduan.project_manager_api.service.PaymentService;

import lombok.extern.slf4j.Slf4j;
import vn.payos.model.webhooks.Webhook;

/**
 * Controller xu ly cac nghiep vu lien quan den thanh toan va giao dich qua PayOS.
 */
@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentController {

    // Khai bao cac hang so mac dinh
    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_SIZE = "10";
    private static final String DEFAULT_STATUS_FILTER = "ALL";

    // Khai bao cac thong bao tra ve (Response Messages)
    private static final String MSG_CHECKOUT_SUCCESS = "Payment link created successfully.";
    private static final String MSG_WEBHOOK_SUCCESS = "Webhook processed successfully.";
    private static final String MSG_WEBHOOK_ERROR_IGNORED = "Error but ignored.";
    private static final String MSG_CANCEL_SUCCESS = "Transaction cancelled successfully.";
    private static final String MSG_FETCH_HISTORY_SUCCESS = "Transaction history retrieved successfully.";
    private static final String MSG_FETCH_DETAIL_SUCCESS = "Transaction detail retrieved successfully.";

    private final PaymentService paymentService;

    // Khoi tao thu cong de tiem phu thuoc
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Tao lien ket thanh toan cho goi cuoc SaaS.
     */
    @PostMapping("/checkout")
    @PreAuthorize("@securityService.hasCompanyPermission(#request.companyId, 'company:manage_billing')")
    public ResponseEntity<ApiResponse<CheckoutResponse>> createCheckoutLink(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Integer currentUserId = currentUser.getId();
        CheckoutResponse response = paymentService.createPaymentLink(request.getCompanyId(), currentUserId, request);

        return ResponseEntity.ok(ApiResponse.success(MSG_CHECKOUT_SUCCESS, response));
    }

    /**
     * Tiep nhan va xu ly tin hieu thanh toan tu PayOS Webhook.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handlePayOSWebhook(@RequestBody String rawBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            Webhook webhookBody = mapper.readValue(rawBody, Webhook.class);
            
            paymentService.processWebhook(webhookBody);
            
            response.put("success", true);
            response.put("message", MSG_WEBHOOK_SUCCESS);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing PayOS webhook: {}", e.getMessage());
            
            // Tra ve success true theo yeu cau cua PayOS de tranh gui lai webhook khi da co loi logic
            response.put("success", true); 
            response.put("message", MSG_WEBHOOK_ERROR_IGNORED);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Huy giao dich dang o trang thai cho thanh toan.
     */
    @PostMapping("/checkout/{transactionCode}/cancel")
    @PreAuthorize("@securityService.hasCompanyPermission(#request.companyId, 'company:manage_billing')")
    public ResponseEntity<ApiResponse<String>> cancelPaymentLink(
            @PathVariable String transactionCode,
            @Valid @RequestBody CancelPaymentRequest request) {

        paymentService.cancelPendingTransaction(transactionCode, request.getCompanyId(), request.getCancellationReason());

        return ResponseEntity.ok(ApiResponse.success(MSG_CANCEL_SUCCESS, null));
    }

    /**
     * Lay danh sach lich su giao dich cua cong ty co phan trang va bo loc.
     */
    @GetMapping("/checkout/history")
    @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:manage_billing')")
    public ResponseEntity<ApiResponse<PageResponseDTO<TransactionListResponse>>> getTransactionHistory(
            @RequestParam Integer companyId,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false, defaultValue = DEFAULT_STATUS_FILTER) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        PageResponseDTO<TransactionListResponse> history = paymentService.getTransactionHistory(
                companyId, page, size, status, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_HISTORY_SUCCESS, history));
    }

    /**
     * Xem thong tin chi tiet cua mot ma giao dich cu the.
     */
    @GetMapping("/checkout/history/{transactionCode}")
    @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:manage_billing')")
    public ResponseEntity<ApiResponse<TransactionDetailResponse>> getTransactionDetail(
            @PathVariable String transactionCode,
            @RequestParam Integer companyId) {

        TransactionDetailResponse detail = paymentService.getTransactionDetail(transactionCode, companyId);
        
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_DETAIL_SUCCESS, detail));
    }
}