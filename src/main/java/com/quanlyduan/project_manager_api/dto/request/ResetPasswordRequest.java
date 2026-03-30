package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de thiet lap lai mat khau moi.
 * Duoc su dung sau khi nguoi dung xac thuc thanh cong thong qua duong dan tu Email.
 */
@Getter
@Setter
public class ResetPasswordRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int MIN_PASSWORD_LENGTH = 6;

    public static final String TOKEN_BLANK_MSG = "Reset token must not be blank";
    public static final String PASSWORD_BLANK_MSG = "New password must not be blank";
    public static final String PASSWORD_SIZE_MSG = "New password must contain at least " + MIN_PASSWORD_LENGTH + " characters";

    // ======================================================
    // 1. THONG TIN XAC THUC (AUTHENTICATION)
    // ======================================================

    /**
     * Token xac thuc quyen dat lai mat khau.
     * Ma nay duoc trich xuat tu link ma he thong da gui vao Email cua nguoi dung.
     */
    @NotBlank(message = TOKEN_BLANK_MSG)
    private String token;

    // ======================================================
    // 2. THONG TIN BAO MAT MOI (NEW SECURITY)
    // ======================================================

    /**
     * Mat khau moi nguoi dung muon thiet lap.
     * Bat buoc phai dat do dai toi thieu de dam bao an toan cho tai khoan.
     */
    @NotBlank(message = PASSWORD_BLANK_MSG)
    @Size(min = MIN_PASSWORD_LENGTH, message = PASSWORD_SIZE_MSG)
    private String newPassword;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh ho tro viec Deserialize JSON tu Client.
     */
    public ResetPasswordRequest() {
    }
}