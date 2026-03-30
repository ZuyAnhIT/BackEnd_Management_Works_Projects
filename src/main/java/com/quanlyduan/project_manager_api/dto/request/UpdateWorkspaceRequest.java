package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat thong tin Khong gian lam viec (Workspace).
 * Ho tro co che Partial Update: Chi nhung truong co gia tri (khong null) moi duoc cap nhat vao he thong.
 */
@Getter
@Setter
public class UpdateWorkspaceRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int NAME_MIN_SIZE = 1;
    public static final int NAME_MAX_SIZE = 255;
    
    public static final String NAME_SIZE_MSG = "Workspace name must be between " 
            + NAME_MIN_SIZE + " and " + NAME_MAX_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN CO BAN (BASIC INFO)
    // ======================================================

    /**
     * Ten moi cua Khong gian lam viec.
     */
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE, message = NAME_SIZE_MSG)
    private String name;

    /**
     * Mo ta chi tiet moi ve muc dich hoac phong ban su dung Workspace nay.
     */
    private String description;

    // ======================================================
    // 2. NHAN DIEN & UI (BRANDING & DISPLAY)
    // ======================================================

    /**
     * Duong dan URL den anh bia (Cover Image) moi.
     */
    private String coverImage;

    /**
     * Ma mau HEX moi hien thi cho Workspace (vi du: #3498db).
     */
    private String color;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public UpdateWorkspaceRequest() {
    }
}