package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu cho hanh dong di chuyen Task sang mot cot trang thai khac.
 * Thuong duoc su dung khi nguoi dung thuc hien thao tac keo tha (Drag & Drop) tren bang cong viec (Board).
 */
@Getter
@Setter
public class MoveTaskStatusRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String STATUS_ID_NULL_MSG = "New status ID must not be null";

    // ======================================================
    // 1. THONG TIN DICH DEN (TARGET DESTINATION)
    // ======================================================

    /**
     * ID cua trang thai (cot) moi ma Task se duoc chuyen den.
     * Bat buoc phai co de backend xac dinh duoc dich den cua Task.
     */
    @NotNull(message = STATUS_ID_NULL_MSG)
    private Integer newStatusId;

    // ======================================================
    // 2. THONG TIN SAP XEP (ORDERING)
    // ======================================================

    /**
     * Vi tri sap xep (Index) mong muon cua Task sau khi tha vao cot moi (vi du: 0, 1, 2...).
     * Neu de null, he thong se mac dinh day Task nay xuong vi tri cuoi cung cua cot do.
     */
    private Integer newSortOrder;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu chuoi JSON.
     */
    public MoveTaskStatusRequest() {
    }
}