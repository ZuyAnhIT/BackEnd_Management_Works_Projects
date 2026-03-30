package com.quanlyduan.project_manager_api.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat thong tin Sprint.
 * Ho tro co che Partial Update: Chi nhung truong co gia tri (khong null) moi duoc cap nhat vao he thong.
 */
@Getter
@Setter
public class UpdateSprintRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int NAME_MIN_SIZE = 1;
    public static final int NAME_MAX_SIZE = 255;
    
    public static final String NAME_SIZE_MSG = "Sprint name must be between " 
            + NAME_MIN_SIZE + " and " + NAME_MAX_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN COT LOI (CORE INFO)
    // ======================================================

    /**
     * Ten moi cua Sprint.
     * Thuong duoc dat theo so thu tu hoac muc tieu chinh (vi du: Sprint 1, Sprint Alpha).
     */
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE, message = NAME_SIZE_MSG)
    private String name;

    /**
     * Muc tieu moi cua Sprint.
     * Mo ta ngan gon nhung gi nhom can dat duoc trong chu ky nay.
     */
    private String goal;

    // ======================================================
    // 2. KE HOACH THOI GIAN (TIMELINE)
    // ======================================================

    /**
     * Thoi gian bat dau moi cua Sprint.
     * Bao gom ca gio va phut (Dinh dang ISO: YYYY-MM-DDTHH:mm:ss).
     */
    private LocalDateTime startDate;

    /**
     * Thoi gian ket thuc du kien moi cua Sprint.
     */
    private LocalDateTime endDate;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu chuoi JSON.
     */
    public UpdateSprintRequest() {
    }
}