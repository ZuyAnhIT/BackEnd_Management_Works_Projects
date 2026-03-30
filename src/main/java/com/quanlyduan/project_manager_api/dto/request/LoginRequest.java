package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan yeu cau dang nhap truyen thong tu phia nguoi dung.
 * Bao gom thong tin dinh danh (Email) va thong tin xac thuc (Mat khau).
 */
@Getter
@Setter
public class LoginRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String EMAIL_BLANK_MSG = "Email must not be blank";
    public static final String EMAIL_INVALID_MSG = "Invalid email format";
    public static final String PASSWORD_BLANK_MSG = "Password must not be blank";

    // ======================================================
    // THONG TIN DANG NHAP (LOGIN DATA)
    // ======================================================

    /**
     * Dia chi Email su dung de dang nhap.
     * Bat buoc phai dung dinh dang email tieu chuan (vi du: user@example.com).
     */
    @NotBlank(message = EMAIL_BLANK_MSG)
    @Email(message = EMAIL_INVALID_MSG)
    private String email;

    /**
     * Mat khau dang nhap cua tai khoan.
     * Bat buoc phai co de backend thuc hien so sanh va bam (hash).
     */
    @NotBlank(message = PASSWORD_BLANK_MSG)
    private String password;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public LoginRequest() {
    }
}