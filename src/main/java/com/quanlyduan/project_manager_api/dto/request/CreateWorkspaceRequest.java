package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu Client de tao moi mot Khong gian lam viec (Workspace).
 * Workspace la noi quan ly tap trung cac du an va thanh vien thuoc mot phong ban hoac linh vuc cu the.
 */
@Getter
@Setter
public class CreateWorkspaceRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String NAME_BLANK_MSG = "Workspace name must not be blank";

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTIFICATION)
    // ======================================================

    /**
     * Ten cua Khong gian lam viec.
     * Bat buoc phai co de khoi tao va hien thi tren danh sach Workspace.
     */
    @NotBlank(message = NAME_BLANK_MSG)
    private String workspaceName;

    /**
     * Mo ta chi tiet ve muc dich su dung hoac cac quy dinh trong Workspace nay.
     */
    private String description;

    // ======================================================
    // 2. THONG TIN HIEN THI (UI/DISPLAY)
    // ======================================================

    /**
     * Duong dan URL den anh bia (Cover Image) cua Workspace.
     * Thuong la link tu Cloud Storage hoac link anh mau he thong cung cap.
     */
    private String coverImage;

    /**
     * Ma mau HEX (vi du: #3498db) de dai dien cho Workspace tren giao dien nguoi dung.
     */
    private String color;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public CreateWorkspaceRequest() {
    }
}