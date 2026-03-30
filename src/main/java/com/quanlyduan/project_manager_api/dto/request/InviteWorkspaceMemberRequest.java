package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de moi thanh vien tham gia vao Khong gian lam viec (Workspace).
 * Luu y: Nguoi duoc moi bat buoc phai la thanh vien cua Cong ty quan ly Workspace nay.
 */
@Getter
@Setter
public class InviteWorkspaceMemberRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String EMAIL_BLANK_MSG = "Email must not be blank";
    public static final String EMAIL_INVALID_MSG = "Invalid email format";
    public static final String ROLE_CODE_BLANK_MSG = "Workspace role code must not be blank";

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTIFICATION)
    // ======================================================

    /**
     * Dia chi Email cua nguoi duoc moi vao Workspace.
     * Bat buoc phai trung khop voi Email thanh vien da ton tai trong Cong ty.
     */
    @NotBlank(message = EMAIL_BLANK_MSG)
    @Email(message = EMAIL_INVALID_MSG)
    private String email;

    // ======================================================
    // 2. THONG TIN PHAN QUYEN (AUTHORIZATION)
    // ======================================================

    /**
     * Ma vai tro cap Workspace (Workspace Role) se phan quyen cho nguoi dung.
     * Vi du: WORKSPACE_ADMIN, WORKSPACE_MEMBER.
     */
    @NotBlank(message = ROLE_CODE_BLANK_MSG)
    private String roleCode;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh ho tro viec Deserialize JSON tu Client.
     */
    public InviteWorkspaceMemberRequest() {
    }
}