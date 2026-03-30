package com.quanlyduan.project_manager_api.dto.request;

import java.time.LocalDate;

import com.quanlyduan.project_manager_api.model.common.enums.Gender;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat thong tin ca nhan (Profile) cua nguoi dung.
 * Ho tro co che Partial Update: Chi nhung truong co gia tri (khong null) moi duoc cap nhat.
 */
@Getter
@Setter
public class UpdateProfileRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int NAME_MIN_SIZE = 3;
    public static final int NAME_MAX_SIZE = 255;
    public static final int PHONE_MAX_SIZE = 20;

    public static final String NAME_SIZE_MSG = "Full name must be between " 
            + NAME_MIN_SIZE + " and " + NAME_MAX_SIZE + " characters";
    public static final String PHONE_SIZE_MSG = "Phone number must not exceed " + PHONE_MAX_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY)
    // ======================================================

    /**
     * Ho va ten hien thi moi cua nguoi dung.
     */
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE, message = NAME_SIZE_MSG)
    private String fullName;

    /**
     * Duong dan URL den anh dai dien (Avatar) moi.
     * Thuong duoc cap nhat sau khi upload file len Cloud Storage.
     */
    private String avatarUrl;

    // ======================================================
    // 2. THONG TIN LIEN LAC (CONTACT)
    // ======================================================

    /**
     * So dien thoai lien he moi.
     */
    @Size(max = PHONE_MAX_SIZE, message = PHONE_SIZE_MSG)
    private String phoneNumber;

    // ======================================================
    // 3. THONG TIN CHI TIET (PERSONAL DETAILS)
    // ======================================================

    /**
     * Ngay sinh cua nguoi dung.
     */
    private LocalDate dateOfBirth;

    /**
     * Gioi tinh (Enum: MALE, FEMALE, OTHER).
     */
    private Gender gender;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu chuoi JSON.
     */
    public UpdateProfileRequest() {
    }
}