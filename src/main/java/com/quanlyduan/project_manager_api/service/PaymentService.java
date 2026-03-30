package com.quanlyduan.project_manager_api.service;

import java.time.LocalDateTime;

import com.quanlyduan.project_manager_api.dto.request.CheckoutRequest;
import com.quanlyduan.project_manager_api.dto.response.CheckoutResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.TransactionDetailResponse;
import com.quanlyduan.project_manager_api.dto.response.TransactionListResponse;

import vn.payos.model.webhooks.Webhook;

/**
 * Service quan ly toan bo nghiep vu Thanh toan va tich hop Cong thanh toan (PayOS).
 * Xu ly luong tao don hang, xac nhan giao dich tu dong qua Webhook va tra cuu lich su tai chinh.
 */
public interface PaymentService {

    // ======================================================
    // 1. KHOI TAO GIAO DICH (TRANSACTION INITIATION)
    // ======================================================

    /**
     * Khoi tao don hang va tao duong dan thanh toan (Checkout URL).
     * Cung cap ma QR Code de khach hang quet thuc hien thanh toan qua ngan hang.
     * * @param companyId ID cong ty thuc hien mua goi
     * @param userId ID nguoi dung thao tac
     * @param request Thong tin goi cuoc va chu ky thanh toan
     * @return Thong tin don hang kem theo URL thanh toan tu PayOS
     */
    CheckoutResponse createPaymentLink(Integer companyId, Integer userId, CheckoutRequest request);

    // ======================================================
    // 2. XU LY PHAN HOI (CALLBACK & WEBHOOK)
    // ======================================================

    /**
     * Tiep nhan va xu ly tin nhan Webhook tu phia PayOS.
     * Thuc hien doi soat chu ky, cap nhat trang thai giao dich va kich hoat goi cuoc cho Cong ty.
     * * @param webhookBody Du lieu thong bao giao dich tu PayOS
     */
    void processWebhook(Webhook webhookBody);

    /**
     * Huy bo mot giao dich dang o trang thai cho (PENDING).
     * * @param transactionCode Ma don hang noi bo
     * @param companyId ID cong ty so huu giao dich
     * @param reason Ly do huy bo giao dich
     */
    void cancelPendingTransaction(String transactionCode, Integer companyId, String reason);

    // ======================================================
    // 3. TRA CUU TAI CHINH (FINANCIAL AUDIT)
    // ======================================================

    /**
     * Truy xuat danh sach lich su giao dich cua mot cong ty.
     * Ho tro loc theo trang thai va khoang thoi gian cu the.
     * * @param companyId ID cong ty can tra cuu
     * @param page So trang hien tai
     * @param size So luong ban ghi tren moi trang
     * @return Trang danh sach cac giao dich tai chinh
     */
    PageResponseDTO<TransactionListResponse> getTransactionHistory(
            Integer companyId, 
            int page, 
            int size, 
            String status, 
            LocalDateTime startDate, 
            LocalDateTime endDate
    );

    /**
     * Xem thong tin chi tiet cua mot ma don hang cu the.
     * * @param transactionCode Ma don hang noi bo
     * @param companyId ID cong ty (dung de xac thuc quyen truy cap)
     * @return Chi tiet thong so giao dich va phan hoi tu cong thanh toan
     */
    TransactionDetailResponse getTransactionDetail(
            String transactionCode, 
            Integer companyId
    );
}