package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de gui loi moi thanh vien tham gia vao he thong.
 * Du lieu nay duoc su dung de khoi tao ban ghi loi moi (Invitation) va gui email thong bao.
 */
@Getter
@Setter
public class InviteMemberRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String EMAIL_BLANK_MSG = "Email must not be blank";
    public static final String EMAIL_INVALID_MSG = "Invalid email format";
    public static final String ROLE_CODE_BLANK_MSG = "Role code must not be blank";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * Dia chi Email cua nguoi duoc moi.
     * Bat buoc phai dung dinh dang email tieu chuan de he thong gui thu moi.
     */
    @NotBlank(message = EMAIL_BLANK_MSG)
    @Email(message = EMAIL_INVALID_MSG)
    private String email;

    /**
     * Ma vai tro (Role Code) du kien phan quyen cho nguoi dung.
     * Vi du: COMPANY_ADMIN, PROJECT_MANAGER.
     */
    @NotBlank(message = ROLE_CODE_BLANK_MSG)
    private String roleCode;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh ho tro viec Deserialize JSON tu Client.
     */
    public InviteMemberRequest() {
    }
}