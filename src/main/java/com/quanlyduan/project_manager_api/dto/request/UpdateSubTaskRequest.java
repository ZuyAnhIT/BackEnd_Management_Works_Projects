package com.quanlyduan.project_manager_api.dto.request;

import java.math.BigDecimal;

import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat thong tin SubTask (Cong viec phu).
 * Ho tro co che Partial Update: Chi nhung truong co gia tri (khong null) moi duoc cap nhat vao he thong.
 */
@Getter
@Setter
public class UpdateSubTaskRequest {

    // ======================================================
    // KHAI BAO HANG SO - DOCUMENTATION (RULE 6)
    // ======================================================
    public static final String TITLE_DESC = "New title of the subtask";
    public static final String DESC_DESC = "New detailed description";
    public static final String STATUS_DESC = "New status of the subtask (TO_DO, IN_PROGRESS, DONE)";
    public static final String ASSIGNEE_DESC = "New assignee ID (User ID)";
    public static final String EST_HOURS_DESC = "New estimated hours to complete";

    // ======================================================
    // 1. THONG TIN NOI DUNG (CONTENT INFO)
    // ======================================================
    
    /**
     * Tieu de moi cua cong viec phu.
     */
    @Schema(description = TITLE_DESC, nullable = true)
    private String title;

    /**
     * Mo ta chi tiet moi ve cach thuc hien hoac ghi chu.
     */
    @Schema(description = DESC_DESC, nullable = true)
    private String description;

    // ======================================================
    // 2. THONG TIN THUC THI (EXECUTION INFO)
    // ======================================================

    /**
     * Trang thai moi cua SubTask (Enum: TO_DO, IN_PROGRESS, DONE).
     */
    @Schema(description = STATUS_DESC, nullable = true)
    private SubTaskStatus status;

    /**
     * ID nguoi dung moi duoc giao thuc hien cong viec nay.
     */
    @Schema(description = ASSIGNEE_DESC, nullable = true)
    private Integer assigneeId;

    // ======================================================
    // 3. CHI SO DO LUONG (METRICS)
    // ======================================================

    /**
     * Thoi gian uoc tinh moi de hoan thanh (don vi: Gio).
     */
    @Schema(description = EST_HOURS_DESC, nullable = true)
    private BigDecimal estimatedHours;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public UpdateSubTaskRequest() {
    }
}