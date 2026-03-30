package com.quanlyduan.project_manager_api.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu cho hanh dong di chuyen Task vao/ra khoi Sprint.
 * Ho tro: 
 * 1. Backlog -> Sprint.
 * 2. Sprint A -> Sprint B.
 * 3. Sprint -> Backlog.
 * 4. Thay doi thu tu (Reorder) trong danh sach.
 */
@Getter
@Setter
public class UpdateTaskSprintRequest {

    // ======================================================
    // 1. THONG TIN DICH DEN (TARGET DESTINATION)
    // ======================================================

    /**
     * ID cua Sprint dich ma Task se duoc chuyen den.
     * - Co gia tri: Chuyen Task vao Sprint tuong ung.
     * - NULL: Chuyen Task ve Backlog (Go khoi moi Sprint).
     */
    private Integer sprintId;

    // ======================================================
    // 2. THONG TIN THU TU (ORDERING)
    // ======================================================

    /**
     * Vi tri sap xep moi (Index) cua Task trong danh sach dich (0, 1, 2...).
     * Neu de null, he thong se mac dinh day Task xuong cuoi danh sach.
     */
    private Integer newSortOrder;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public UpdateTaskSprintRequest() {
    }
}