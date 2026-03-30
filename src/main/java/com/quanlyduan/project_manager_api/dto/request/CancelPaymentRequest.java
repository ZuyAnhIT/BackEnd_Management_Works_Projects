package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan yeu cau huy giao dich thanh toan tu phia nguoi dung.
 * Ghi nhan ID cong ty va ly do huy de phuc vu viec doi soat hoac thong ke cham soc khach hang.
 */
@Getter
@Setter
public class CancelPaymentRequest {

    // ======================================================
    // KHAI BAO HANG SO
    // ======================================================
    
    public static final String COMPANY_ID_NOT_NULL_MSG = "Company ID must not be null";
    public static final String DEFAULT_CANCELLATION_REASON = "User initiated payment cancellation";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * ID cua cong ty thuc hien giao dich.
     */
    @NotNull(message = COMPANY_ID_NOT_NULL_MSG)
    private Integer companyId;

    /**
     * Ly do huy thanh toan do Frontend gui len (vi du: Doi y, Chon sai goi).
     * Neu khong co, he thong se su dung gia tri mac dinh.
     */
    private String cancellationReason = DEFAULT_CANCELLATION_REASON;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh ho tro Spring/Jackson map du lieu tu JSON.
     */
    public CancelPaymentRequest() {
    }
}