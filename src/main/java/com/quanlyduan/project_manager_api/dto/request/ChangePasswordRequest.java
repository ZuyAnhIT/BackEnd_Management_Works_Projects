package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan yeu cau doi mat khau tu phia nguoi dung.
 * Yeu cau xac thuc bang mat khau cu truoc khi thiet lap mat khau moi.
 */
@Getter
@Setter
public class ChangePasswordRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final String OLD_PASS_BLANK_MSG = "Old password must not be blank";
    public static final String NEW_PASS_BLANK_MSG = "New password must not be blank";
    public static final String CONFIRM_PASS_BLANK_MSG = "Confirm new password must not be blank";
    public static final String PASS_SIZE_MSG = "New password must contain at least " + MIN_PASSWORD_LENGTH + " characters";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * Mat khau hien tai cua nguoi dung.
     * Bat buoc de xac minh danh tinh truoc khi cho phep thay doi.
     */
    @NotBlank(message = OLD_PASS_BLANK_MSG)
    private String oldPassword;

    /**
     * Mat khau moi muon thiet lap.
     * Yeu cau do dai toi thieu de dam bao tinh bao mat.
     */
    @NotBlank(message = NEW_PASS_BLANK_MSG)
    @Size(min = MIN_PASSWORD_LENGTH, message = PASS_SIZE_MSG)
    private String newPassword;

    /**
     * Nhap lai mat khau moi de xac nhan.
     * Logic kiem tra khop (matching) se duoc xu ly tai tang Service.
     */
    @NotBlank(message = CONFIRM_PASS_BLANK_MSG)
    private String confirmNewPassword;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON.
     */
    public ChangePasswordRequest() {
    }
}