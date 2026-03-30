package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan yeu cau khoi phuc mat khau tu phia nguoi dung.
 * Nguoi dung cung cap email de he thong kiem tra va gui lien ket dat lai mat khau.
 */
@Getter
@Setter
public class ForgotPasswordRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String EMAIL_BLANK_MSG = "Email must not be blank";
    public static final String EMAIL_INVALID_MSG = "Invalid email format";

    // ======================================================
    // THONG TIN DINH DANH (IDENTIFICATION)
    // ======================================================

    /**
     * Dia chi Email cua nguoi dung can khoi phuc mat khau.
     * Bat buoc phai dung dinh dang email tieu chuan (vi du: user@example.com).
     */
    @NotBlank(message = EMAIL_BLANK_MSG)
    @Email(message = EMAIL_INVALID_MSG)
    private String email;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh ho tro viec Deserialize JSON tu Client.
     */
    public ForgotPasswordRequest() {
    }
}