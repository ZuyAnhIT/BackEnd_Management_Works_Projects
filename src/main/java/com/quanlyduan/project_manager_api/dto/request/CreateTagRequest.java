package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu Client de tao moi mot The (Tag) trong du an.
 * Tag duoc dung de gan nhan, phan loai va loc cac cong viec (Task) mot cach nhanh chong.
 */
@Getter
@Setter
public class CreateTagRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String NAME_BLANK_MSG = "Tag name must not be blank";

    // ======================================================
    // 1. THONG TIN HIEN THI (UI/DISPLAY)
    // ======================================================

    /**
     * Ten cua the (Tag name).
     * Bat buoc phai co de nguoi dung co the nhan biet va tim kiem.
     */
    @NotBlank(message = NAME_BLANK_MSG)
    private String name;

    /**
     * Ma mau HEX (vi du: #FF5733) de hien thi nhan tren giao dien.
     * Neu de trong, he thong se tu dong gan mau mac dinh.
     */
    private String color;

    // ======================================================
    // 2. THONG TIN CHI TIET (DETAILS)
    // ======================================================

    /**
     * Mo ta chi tiet ve y nghia hoac quy uoc su dung cua the nay.
     */
    private String description;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu JSON.
     */
    public CreateTagRequest() {
    }
}