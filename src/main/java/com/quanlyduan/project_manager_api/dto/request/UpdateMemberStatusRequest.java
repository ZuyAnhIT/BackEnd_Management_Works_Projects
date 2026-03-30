package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat trang thai hoat dong cua thanh vien.
 * Duoc su dung cho cac hanh dong nhu Tam dung (Suspend) hoac Kich hoat lai (Activate).
 */
@Getter
@Setter
public class UpdateMemberStatusRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String STATUS_NULL_MSG = "New status must not be null";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * Trang thai moi muon ap dung cho thanh vien (vi du: ACTIVE, SUSPENDED).
     * Bat buoc phai co de backend thuc hien cap nhat.
     * Luu y: Gia tri REMOVED thuong bi chan tai Service vi co API xoa rieng.
     */
    @NotNull(message = STATUS_NULL_MSG)
    private MemberStatus newStatus;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu chuoi JSON.
     */
    public UpdateMemberStatusRequest() {
    }
}