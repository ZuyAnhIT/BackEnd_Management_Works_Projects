package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi sau khi nguoi dung dang nhap thanh cong.
 * Chua cac thong tin xac thuc (JWT Tokens) de Client luu tru va su dung cho cac request tiep theo.
 */
@Getter
@Setter
@Builder
public class LoginResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String DEFAULT_TOKEN_TYPE = "Bearer";

    // ======================================================
    // 1. THONG TIN XAC THUC (AUTHENTICATION TOKENS)
    // ======================================================

    /** * Access Token (Token truy cap).
     * Dung de xac thuc trong Header moi request (Authorization: Bearer <token>).
     * Thuong co thoi han ngan (vi du: 1 gio).
     */
    private String accessToken;

    /** * Refresh Token (Token lam moi).
     * Dung de cap lai Access Token moi khi cai cu het han ma khong can dang nhap lai.
     * Thuong co thoi han dai (vi du: 7 ngay) va can duoc luu tru bao mat.
     */
    private String refreshToken;

    // ======================================================
    // 2. CAU HINH TOKEN (TOKEN CONFIGURATION)
    // ======================================================

    /** * Loai Token (Mac dinh la "Bearer").
     * Client se ghep chuoi nay voi Access Token khi gui request len Server.
     */
    private String tokenType;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Giup Jackson Deserialize du lieu mot cach minh bach.
     */
    public LoginResponse() {
        // Khoi tao gia tri mac dinh cho tokenType
        this.tokenType = DEFAULT_TOKEN_TYPE;
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public LoginResponse(String accessToken, String refreshToken, String tokenType) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = (tokenType != null) ? tokenType : DEFAULT_TOKEN_TYPE;
    }
}