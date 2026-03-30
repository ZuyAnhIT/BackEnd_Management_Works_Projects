package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de moi thanh vien tham gia vao mot Du an (Project).
 * Ap dung cho ca viec phan cong thanh vien noi bo va moi khach (Guest) tu ben ngoai.
 */
@Getter
@Setter
public class InviteProjectMemberRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String EMAIL_BLANK_MSG = "Email must not be blank";
    public static final String EMAIL_INVALID_MSG = "Invalid email format";
    public static final String ROLE_CODE_BLANK_MSG = "Project role code must not be blank";

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTIFICATION)
    // ======================================================

    /**
     * Dia chi Email cua nguoi duoc moi vao du an.
     * Bat buoc phai dung dinh dang email tieu chuan de he thong gui thong bao hoac loi moi.
     */
    @NotBlank(message = EMAIL_BLANK_MSG)
    @Email(message = EMAIL_INVALID_MSG)
    private String email;

    // ======================================================
    // 2. THONG TIN PHAN QUYEN (AUTHORIZATION)
    // ======================================================

    /**
     * Ma vai tro cap Du an (Project Role) se phan quyen cho nguoi dung.
     * Vi du: PROJECT_ADMIN, PROJECT_MEMBER, GUEST_PROJECT.
     */
    @NotBlank(message = ROLE_CODE_BLANK_MSG)
    private String roleCode;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh ho tro viec Deserialize JSON tu Client.
     */
    public InviteProjectMemberRequest() {
    }
}