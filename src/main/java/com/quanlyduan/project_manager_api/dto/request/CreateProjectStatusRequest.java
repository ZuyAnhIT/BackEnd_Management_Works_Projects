package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu Client de tao moi mot Trang thai du an.
 * Dai dien cho mot cot (Column) tren bang cong viec (Board) cua du an.
 */
@Getter
@Setter
public class CreateProjectStatusRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int NAME_MAX_SIZE = 100;
    
    public static final String NAME_BLANK_MSG = "Status name must not be blank";
    public static final String NAME_SIZE_MSG = "Status name must not exceed " + NAME_MAX_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN HIEN THI (UI/DISPLAY)
    // ======================================================

    /**
     * Ten trang thai hien thi tren cot cua Board.
     * Bat buoc phai co de nguoi dung phan biet cac giai doan cong viec.
     */
    @NotBlank(message = NAME_BLANK_MSG)
    @Size(max = NAME_MAX_SIZE, message = NAME_SIZE_MSG)
    private String name;

    /**
     * Ma mau HEX (vi du: #3498db) de phan biet cac trang thai tren giao dien.
     */
    private String color;

    // ======================================================
    // 2. LOGIC HE THONG (SYSTEM LOGIC)
    // ======================================================

    /**
     * Co danh dau trang thai nay co duoc coi la da hoan thanh hay khong.
     * Giup he thong tinh toan tien do (Progress) cua Project hoac Sprint.
     */
    private Boolean isCompletedStatus;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public CreateProjectStatusRequest() {
    }
}