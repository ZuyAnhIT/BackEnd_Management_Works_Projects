package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat vai tro (Role) cua mot thanh vien.
 * Duoc su dung chung cho cac cap do: Cong ty, Khong gian lam viec, va Du an.
 */
@Getter
@Setter
public class RoleUpdateRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String ROLE_BLANK_MSG = "Role code must not be blank";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * Ma vai tro moi muon gan cho thanh vien.
     * Vi du: "COMPANY_ADMIN", "PROJECT_MEMBER", "GUEST_PROJECT".
     * Bat buoc phai co de backend thuc hien phan quyen lai.
     */
    @NotBlank(message = ROLE_BLANK_MSG)
    private String roleCode;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu JSON.
     */
    public RoleUpdateRequest() {
    }
}