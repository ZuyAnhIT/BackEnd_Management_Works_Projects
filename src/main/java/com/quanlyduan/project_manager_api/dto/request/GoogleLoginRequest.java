package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu cho yeu cau dang nhap bang tai khoan Google (OAuth2/OIDC).
 * Frontend gui ID Token nhan duoc tu Google len Server de Backend thuc hien xac minh danh tinh.
 */
@Getter
@Setter
public class GoogleLoginRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String TOKEN_BLANK_MSG = "Google token must not be blank";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * Chuoi ID Token (dinh dang JWT) do Google cung cap cho Client.
     * Backend su dung chuoi nay de xac thuc nguoi dung voi Google Authorization Server.
     * Bat buoc phai co de hoan tat quy trinh dang nhap.
     */
    @NotBlank(message = TOKEN_BLANK_MSG)
    private String googleToken;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu chuoi JSON.
     */
    public GoogleLoginRequest() {
    }
}