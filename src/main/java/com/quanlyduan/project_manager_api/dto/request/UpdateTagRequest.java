package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat thong tin The (Tag).
 * Ho tro co che Partial Update: Chi nhung truong co gia tri (khong null) moi duoc cap nhat vao he thong.
 */
@Getter
@Setter
public class UpdateTagRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int NAME_MAX_SIZE = 100;
    public static final int DESC_MAX_SIZE = 255;
    
    public static final String NAME_SIZE_MSG = "Tag name must not exceed " + NAME_MAX_SIZE + " characters";
    public static final String DESC_SIZE_MSG = "Description must not exceed " + DESC_MAX_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN HIEN THI (DISPLAY INFO)
    // ======================================================

    /**
     * Ten moi cua the (vi du: Bug, Feature, Urgent).
     */
    @Size(max = NAME_MAX_SIZE, message = NAME_SIZE_MSG)
    private String name;

    /**
     * Ma mau HEX moi hien thi cho the (vi du: #00AA00).
     */
    private String color;

    // ======================================================
    // 2. THONG TIN MO TA (DESCRIPTION INFO)
    // ======================================================

    /**
     * Mo ta chi tiet moi ve y nghia hoac cach su dung the nay.
     */
    @Size(max = DESC_MAX_SIZE, message = DESC_SIZE_MSG)
    private String description;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public UpdateTagRequest() {
    }
}