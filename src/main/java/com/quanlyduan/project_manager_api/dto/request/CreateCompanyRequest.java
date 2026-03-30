package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu Client de tao moi mot Cong ty (Company).
 * Chua cac thong tin co ban va thong tin lien he ban dau cua to chuc.
 */
@Getter
@Setter
public class CreateCompanyRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int NAME_MIN_SIZE = 3;
    public static final int NAME_MAX_SIZE = 255;
    
    public static final String NAME_BLANK_MSG = "Company name must not be blank";
    public static final String NAME_SIZE_MSG = "Company name must be between " + NAME_MIN_SIZE + " and " + NAME_MAX_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTIFICATION)
    // ======================================================

    /**
     * Ten day du cua cong ty.
     * Bat buoc phai cung cap de khoi tao to chuc.
     */
    @NotBlank(message = NAME_BLANK_MSG)
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE, message = NAME_SIZE_MSG)
    private String companyName;

    /**
     * Mo ta ngan gon ve linh vuc hoac thong tin chung cua cong ty.
     */
    private String description;

    // ======================================================
    // 2. THONG TIN LIEN HE VA DIA CHI (CONTACT & ADDRESS)
    // ======================================================

    /**
     * Dia chi tru so chinh hoac van phong giao dich.
     */
    private String address;

    /**
     * So dien thoai lien he chinh thuc.
     */
    private String phoneNumber;

    /**
     * Dia chi email lien he chung (vi du: info@company.com).
     */
    private String email;

    /**
     * Duong dan den trang web chinh thuc cua cong ty.
     */
    private String website;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public CreateCompanyRequest() {
    }
}