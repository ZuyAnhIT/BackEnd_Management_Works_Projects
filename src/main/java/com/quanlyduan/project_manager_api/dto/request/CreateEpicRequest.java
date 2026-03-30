package com.quanlyduan.project_manager_api.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu Client de tao moi mot Epic (Tinh nang lon).
 * Epic dung de nhom cac Task lien quan nham quan ly tien do theo tung giai doan hoac module.
 */
@Getter
@Setter
public class CreateEpicRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int NAME_MAX_SIZE = 255;
    public static final int DESC_MAX_SIZE = 1000;
    
    public static final String NAME_BLANK_MSG = "Epic name must not be blank";
    public static final String NAME_SIZE_MSG = "Epic name must not exceed " + NAME_MAX_SIZE + " characters";
    public static final String DESC_SIZE_MSG = "Description must not exceed " + DESC_MAX_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTIFICATION)
    // ======================================================

    /**
     * Ten cua Epic.
     * Bat buoc phai co de hien thi tren Roadmap va danh sach Epic.
     */
    @NotBlank(message = NAME_BLANK_MSG)
    @Size(max = NAME_MAX_SIZE, message = NAME_SIZE_MSG)
    private String name;

    /**
     * Mo ta chi tiet ve muc tieu hoac cac tinh nang con ben trong Epic.
     */
    @Size(max = DESC_MAX_SIZE, message = DESC_SIZE_MSG)
    private String description;

    /**
     * Ma mau HEX (vi du: #FF5733) de phan biet cac Epic tren giao dien bieu do.
     * Neu khong cung cap, he thong se tu dong gan mau mac dinh.
     */
    private String color;

    // ======================================================
    // 2. THONG TIN THOI GIAN (TIMELINE)
    // ======================================================

    /**
     * Ngay bat dau du kien trien khai Epic.
     */
    private LocalDate startDate;

    /**
     * Ngay ket thuc du kien (Deadline) cua toan bo Epic.
     */
    private LocalDate dueDate;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public CreateEpicRequest() {
    }
}