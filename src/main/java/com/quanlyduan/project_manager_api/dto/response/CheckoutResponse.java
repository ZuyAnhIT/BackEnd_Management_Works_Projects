package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin thanh toan (Checkout).
 * Cung cap duong dan thanh toan tu Cong thanh toan (vi du: PayOS) va ma giao dich noi bo.
 */
@Getter
@Setter
@Builder
public class CheckoutResponse {

    // ======================================================
    // THONG TIN GIAO DICH (PAYMENT INFO)
    // ======================================================
    
    /** * Ma don hang cua he thong Worknet (Transaction Code).
     * Dung de hien thi cho khach hang va doi chuc voi lich su thanh toan sau nay.
     */
    private String transactionCode; 
    
    /** * Duong link thanh toan do Cong thanh toan tra ve.
     * Frontend se su dung link nay de hien thi ma QR hoac chuyen huong nguoi dung.
     */
    private String checkoutUrl;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Giup cac thu vien Mapping hoac Jackson khoi tao doi tuong tu JSON.
     */
    public CheckoutResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay.
     * Bat buoc phai co de @Builder cua Lombok hoat dong dung cach.
     */
    public CheckoutResponse(String transactionCode, String checkoutUrl) {
        this.transactionCode = transactionCode;
        this.checkoutUrl = checkoutUrl;
    }
}