package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de xac thuc tai khoan (Email Verification).
 * Nguoi dung nhap ma OTP (One-Time Password) da nhan duoc qua email de kich hoat tai khoan.
 */
@Getter
@Setter
public class VerifyEmailRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String EMAIL_BLANK_MSG = "Email must not be blank";
    public static final String EMAIL_FORMAT_MSG = "Invalid email format";
    public static final String OTP_BLANK_MSG = "OTP must not be blank";

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY)
    // ======================================================

    /**
     * Email can xac thuc.
     * Bat buoc phai dung dinh dang email de he thong tra cuu tai khoan chinh xac.
     */
    @NotBlank(message = EMAIL_BLANK_MSG)
    @Email(message = EMAIL_FORMAT_MSG)
    private String email;

    // ======================================================
    // 2. THONG TIN XAC THUC (AUTHENTICATION)
    // ======================================================

    /**
     * Ma OTP (One-Time Password) nguoi dung nhan duoc tu hop thu den.
     */
    @NotBlank(message = OTP_BLANK_MSG)
    private String otp;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public VerifyEmailRequest() {
    }
}