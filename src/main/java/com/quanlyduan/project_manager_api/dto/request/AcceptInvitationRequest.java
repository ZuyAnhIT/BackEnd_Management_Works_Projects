package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan yeu cau chap nhan loi moi tu nguoi dung.
 * Su dung chung cho quy trinh moi vao Cong ty, Khong gian lam viec hoac Du an.
 */
@Getter
@Setter
public class AcceptInvitationRequest {

    // ======================================================
    // KHAI BAO HANG SO
    // ======================================================
    public static final String TOKEN_NOT_BLANK_MSG = "Invitation token must not be blank";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * Chuoi token dinh danh loi moi.
     * Thuong duoc trich xuat tu URL dinh kem trong Email gui den nguoi dung.
     */
    @NotBlank(message = TOKEN_NOT_BLANK_MSG)
    private String invitationToken;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh ho tro Spring/Jackson map du lieu tu JSON.
     */
    public AcceptInvitationRequest() {
    }
}