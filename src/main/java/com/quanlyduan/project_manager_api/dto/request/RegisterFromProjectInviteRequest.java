package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu dang ky tai khoan moi tu loi moi tham gia Du an (Project Invitation).
 * Dac thu cho nguoi dung ben ngoai (Khach/Doi tac) chua co tai khoan tren he thong.
 */
@Getter
@Setter
public class RegisterFromProjectInviteRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int MIN_PASSWORD_LENGTH = 8;
    
    public static final String FULL_NAME_BLANK_MSG = "Full name must not be blank";
    public static final String PASSWORD_BLANK_MSG = "Password must not be blank";
    public static final String TOKEN_BLANK_MSG = "Invitation token must not be blank";
    public static final String PASSWORD_SIZE_MSG = "Password must contain at least " + MIN_PASSWORD_LENGTH + " characters";

    // ======================================================
    // 1. THONG TIN NGUOI DUNG (USER INFO)
    // ======================================================

    /**
     * Ho va ten hien thi cua Guest/Freelancer.
     * Bat buoc phai co de khoi tao profile trong du an.
     */
    @NotBlank(message = FULL_NAME_BLANK_MSG)
    private String fullName;

    /**
     * Mat khau dang nhap cho tai khoan Guest moi.
     * Bat buoc dat do dai toi thieu 8 ky tu de tang cuong bao mat cho nguoi dung ben ngoai.
     */
    @NotBlank(message = PASSWORD_BLANK_MSG)
    @Size(min = MIN_PASSWORD_LENGTH, message = PASSWORD_SIZE_MSG)
    private String password;

    // ======================================================
    // 2. XAC THUC LOI MOI (INVITATION AUTH)
    // ======================================================

    /**
     * Ma Token loi moi du an trich xuat tu Email.
     * Backend dung Token nay de xac dinh du an va vai tro (Guest/Member) duoc chi dinh.
     */
    @NotBlank(message = TOKEN_BLANK_MSG)
    private String invitationToken; 

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public RegisterFromProjectInviteRequest() {
    }
}