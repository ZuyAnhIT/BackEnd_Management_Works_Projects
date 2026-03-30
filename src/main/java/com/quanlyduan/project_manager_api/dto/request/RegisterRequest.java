package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu dang ky tai khoan moi truc tiep tu nguoi dung (Public Registration).
 * Day la buoc dau tien trong luong Onboarding de nguoi dung tu thiet lap tai khoan ca nhan.
 */
@Getter
@Setter
public class RegisterRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int MIN_PASSWORD_LENGTH = 6;
    
    public static final String NAME_BLANK_MSG = "Full name must not be blank";
    public static final String EMAIL_BLANK_MSG = "Email must not be blank";
    public static final String EMAIL_INVALID_MSG = "Invalid email format";
    public static final String PASSWORD_BLANK_MSG = "Password must not be blank";
    public static final String PASSWORD_SIZE_MSG = "Password must contain at least " + MIN_PASSWORD_LENGTH + " characters";

    // ======================================================
    // 1. THONG TIN CA NHAN (PERSONAL INFO)
    // ======================================================

    /**
     * Ho va ten day du cua nguoi dung.
     * Bat buoc phai co de hien thi tren profile va cac hoat dong trong du an.
     */
    @NotBlank(message = NAME_BLANK_MSG)
    private String fullName;

    // ======================================================
    // 2. THONG TIN XAC THUC (AUTHENTICATION)
    // ======================================================

    /**
     * Dia chi Email su dung de dang ky va dang nhap sau nay.
     * Bat buoc dung dinh dang email tieu chuan de Backend gui mail xac nhan (neu co).
     */
    @NotBlank(message = EMAIL_BLANK_MSG)
    @Email(message = EMAIL_INVALID_MSG)
    private String email;

    /**
     * Mat khau dang nhap cho tai khoan moi.
     * Bat buoc dat do dai toi thieu de dam bao an toan co ban cho tai khoan.
     */
    @NotBlank(message = PASSWORD_BLANK_MSG)
    @Size(min = MIN_PASSWORD_LENGTH, message = PASSWORD_SIZE_MSG)
    private String password;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh ho tro viec Deserialize JSON tu Client.
     */
    public RegisterRequest() {
    }
}