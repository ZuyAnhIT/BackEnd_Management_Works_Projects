package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat thong tin Cong ty.
 * Ho tro co che Partial Update: Chi cap nhat nhung truong co gia tri khac null tu Client.
 */
@Getter
@Setter
public class UpdateCompanyRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int MIN_NAME_SIZE = 3;
    public static final int MAX_NAME_SIZE = 255;
    
    public static final String NAME_SIZE_MSG = "Company name must be between " 
            + MIN_NAME_SIZE + " and " + MAX_NAME_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN CO BAN (BASIC INFO)
    // ======================================================

    /**
     * Ten moi cua cong ty.
     * Neu co gia tri, bat buoc phai dat do dai quy dinh.
     */
    @Size(min = MIN_NAME_SIZE, max = MAX_NAME_SIZE, message = NAME_SIZE_MSG)
    private String companyName;

    /**
     * Mo ta moi ve linh vuc hoac quy mo hoat dong.
     */
    private String description;

    // ======================================================
    // 2. NHAN DIEN THUONG HIEU (BRANDING)
    // ======================================================

    /**
     * Duong dan URL den anh Logo moi.
     */
    private String logo;

    /**
     * Dia chi website chinh thuc cua cong ty.
     */
    private String website;

    // ======================================================
    // 3. THONG TIN LIEN HE (CONTACT)
    // ======================================================

    /**
     * Dia chi tru so chinh hoac chi nhanh moi.
     */
    private String address;

    /**
     * So dien thoai lien he moi.
     */
    private String phoneNumber;

    /**
     * Email giao dich chinh thuc moi.
     */
    private String email;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public UpdateCompanyRequest() {
    }
}