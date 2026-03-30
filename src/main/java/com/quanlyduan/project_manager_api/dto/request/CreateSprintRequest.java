package com.quanlyduan.project_manager_api.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu Client de tao moi mot chu ky lam viec (Sprint).
 * Ho tro co che tao nhanh, cho phep de trong cac truong de he thong tu dong tinh toan.
 */
@Getter
@Setter
public class CreateSprintRequest {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTIFICATION)
    // ======================================================

    /**
     * Ten cua Sprint (vi du: Sprint 1).
     * Neu de trong, he thong se tu dong sinh ten theo thu tu tang dan.
     */
    private String name;

    /**
     * Muc tieu cot loi cua team trong chu ky nay (Sprint Goal).
     */
    private String goal;

    // ======================================================
    // 2. THONG TIN THOI GIAN (TIMELINE)
    // ======================================================

    /**
     * Thoi gian bat dau du kien cua Sprint.
     * Dinh dang chuan ISO: YYYY-MM-DDTHH:mm:ss.
     */
    private LocalDateTime startDate;

    /**
     * Thoi gian ket thuc du kien cua Sprint.
     * Neu de trong, se duoc tinh dua tren cau hinh mac dinh cua du an (vi du: +2 tuan).
     */
    private LocalDateTime endDate;

    // ======================================================
    // 3. DU LIEU LIEN KET (RELATIONS)
    // ======================================================

    /**
     * Danh sach ID cua cac Task duoc keo tu Backlog vao Sprint ngay khi khoi tao.
     */
    private List<Integer> taskIds;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Jackson Deserialize du lieu tu JSON mot cach chinh xac.
     */
    public CreateSprintRequest() {
    }
}