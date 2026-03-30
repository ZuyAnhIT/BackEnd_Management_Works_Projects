package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan yeu cau dang xuat (Logout) tu phia Client.
 * Client gui Refresh Token len de Backend thuc hien thu hoi (Revoke), 
 * ngan chan viec tai su dung token de lay Access Token moi.
 */
@Getter
@Setter
public class LogoutRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String TOKEN_BLANK_MSG = "Refresh token must not be blank";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * Chuoi Refresh Token can bi vo hieu hoa.
     * Bat buoc phai co de he thong xac dinh phien lam viec can huy.
     */
    @NotBlank(message = TOKEN_BLANK_MSG)
    private String refreshToken;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu chuoi JSON.
     */
    public LogoutRequest() {
    }
}