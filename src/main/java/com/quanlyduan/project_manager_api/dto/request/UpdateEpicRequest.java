package com.quanlyduan.project_manager_api.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat thong tin Epic.
 * Ho tro co che Partial Update: Chi nhung truong co gia tri (khong null) moi duoc cap nhat vao he thong.
 */
@Getter
@Setter
public class UpdateEpicRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int NAME_MAX_SIZE = 255;
    public static final int DESC_MAX_SIZE = 1000;
    
    public static final String NAME_SIZE_MSG = "Epic name must not exceed " + NAME_MAX_SIZE + " characters";
    public static final String DESC_SIZE_MSG = "Description must not exceed " + DESC_MAX_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTIFICATION)
    // ======================================================

    /**
     * Ten moi cua Epic.
     * (Tuy chon, neu co gia tri thi khong duoc vuot qua 255 ky tu)
     */
    @Size(max = NAME_MAX_SIZE, message = NAME_SIZE_MSG)
    private String name;

    /**
     * Mo ta chi tiet moi ve noi dung hoac muc tieu cua Epic.
     */
    @Size(max = DESC_MAX_SIZE, message = DESC_SIZE_MSG)
    private String description;

    /**
     * Ma mau HEX moi hien thi tren giao dien (vi du: #8e44ad).
     */
    private String color;

    // ======================================================
    // 2. TRANG THAI HE THONG (SYSTEM STATUS)
    // ======================================================

    /**
     * Trang thai moi cua Epic (OPEN, IN_PROGRESS, COMPLETED, CLOSED).
     * Service se thuc hien parse chuoi nay sang Enum tuong ung.
     */
    private String status;

    // ======================================================
    // 3. THONG TIN THOI GIAN (TIMELINE)
    // ======================================================

    /**
     * Ngay bat dau du kien moi.
     */
    private LocalDate startDate;

    /**
     * Ngay ket thuc du kien (Due Date) moi.
     */
    private LocalDate dueDate;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public UpdateEpicRequest() {
    }
}