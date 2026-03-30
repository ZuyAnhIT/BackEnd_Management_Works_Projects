package com.quanlyduan.project_manager_api.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu Client de tao moi mot Cong viec con (SubTask).
 * Ho tro tai lieu Swagger de hien thi mo ta va vi du mau tren API Docs.
 */
@Getter
@Setter
public class CreateSubTaskRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String TITLE_BLANK_MSG = "Subtask title must not be blank";
    public static final String TITLE_DESC = "Title of the subtask";
    public static final String DESC_DESC = "Detailed description of the subtask";
    public static final String ASSIGNEE_DESC = "ID of the user assigned to this subtask (Optional)";
    public static final String ESTIMATED_DESC = "Estimated hours to complete the subtask (e.g. 1.5)";

    // ======================================================
    // 1. THONG TIN NOI DUNG (CONTENT)
    // ======================================================

    /**
     * Tieu de cua SubTask.
     * Bat buoc phai cung cap de xac dinh dau viec.
     */
    @NotBlank(message = TITLE_BLANK_MSG)
    @Schema(description = TITLE_DESC, requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * Mo ta chi tiet cac buoc can thuc hien.
     */
    @Schema(description = DESC_DESC)
    private String description;

    // ======================================================
    // 2. THONG TIN THUC THI (EXECUTION)
    // ======================================================

    /**
     * ID cua nguoi duoc phan cong.
     * Neu de null, SubTask se o trang thai chua co nguoi dam nhan.
     */
    @Schema(description = ASSIGNEE_DESC)
    private Integer assigneeId;

    /**
     * Thoi gian uoc tinh (don vi: Gio).
     * Su dung BigDecimal de ho tro nhap cac gia tri le nhu 1.5 gio hoac 0.5 gio.
     */
    @Schema(description = ESTIMATED_DESC)
    private BigDecimal estimatedHours;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public CreateSubTaskRequest() {
    }
}