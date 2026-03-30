package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan yeu cau dang ky tai khoan moi thong qua loi moi (Invitation).
 * Ap dung khi nguoi dung chua co tai khoan nhung duoc moi tham gia vao mot to chuc.
 */
@Getter
@Setter
public class RegisterFromInviteRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int MIN_PASSWORD_LENGTH = 6;
    
    public static final String FULL_NAME_BLANK_MSG = "Full name must not be blank";
    public static final String PASSWORD_BLANK_MSG = "Password must not be blank";
    public static final String TOKEN_BLANK_MSG = "Invitation token must not be blank";
    public static final String PASSWORD_SIZE_MSG = "Password must contain at least " + MIN_PASSWORD_LENGTH + " characters";

    // ======================================================
    // 1. THONG TIN TAI KHOAN (ACCOUNT INFO)
    // ======================================================

    /**
     * Ho va ten hien thi cua nguoi dung moi.
     * Bat buoc phai co de thiet lap profile ban dau.
     */
    @NotBlank(message = FULL_NAME_BLANK_MSG) 
    private String fullName; 

    /**
     * Mat khau dang nhap cho tai khoan moi.
     * Bat buoc dat do dai toi thieu de dam bao tinh bao mat.
     */
    @NotBlank(message = PASSWORD_BLANK_MSG) 
    @Size(min = MIN_PASSWORD_LENGTH, message = PASSWORD_SIZE_MSG) 
    private String password; 

    // ======================================================
    // 2. XAC THUC LOI MOI (INVITATION AUTHENTICATION)
    // ======================================================

    /**
     * Ma Token loi moi trich xuat tu duong link trong Email.
     * Dung de Backend doi chieu email nguoi dung va thong tin Cong ty/Du an tuong ung.
     */
    @NotBlank(message = TOKEN_BLANK_MSG) 
    private String invitationToken;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public RegisterFromInviteRequest() {
    }
}