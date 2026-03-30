package com.quanlyduan.project_manager_api.dto.request.plan;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu Client de cap nhat thong tin Goi cuoc (Subscription Plan).
 * Cho phep cac truong du lieu la null de ho tro cap nhat mot phan (Partial Update).
 */
@Getter
@Setter
public class UpdatePlanRequest {

    // ======================================================
    // KHAI BAO HANG SO
    // ======================================================
    public static final String PLAN_CODE_REGEX = "^[A-Z0-9_]+$";

    // ======================================================
    // 1. THONG TIN CO BAN (IDENTIFICATION)
    // ======================================================

    @Pattern(regexp = PLAN_CODE_REGEX, message = "Plan code must contain only uppercase letters, numbers, and underscores")
    private String planCode;

    private String name;

    private String description;

    // ======================================================
    // 2. CAU HINH GIA CA (PRICING)
    // ======================================================

    @Min(value = 0, message = "Monthly price cannot be negative")
    private BigDecimal monthlyPrice;

    @Min(value = 0, message = "Yearly price cannot be negative")
    private BigDecimal yearlyPrice;

    // ======================================================
    // 3. GIOI HAN TAI NGUYEN (QUOTA/LIMITS)
    // ======================================================

    @Min(value = -1, message = "Max users must be -1 or greater")
    private Integer maxUsers;

    @Min(value = -1, message = "Max workspaces must be -1 or greater")
    private Integer maxWorkspaces;

    @Min(value = -1, message = "Max projects must be -1 or greater")
    private Integer maxProjects;

    @Min(value = -1, message = "Max storage must be -1 or greater")
    private Integer maxStorageGb;

    // ======================================================
    // 4. TINH NANG VA TRANG THAI (FEATURES & SETTINGS)
    // ======================================================

    private JsonNode features;

    private Boolean isActive;

    private Integer sortOrder;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh ho tro Jackson Deserialize.
     */
    public UpdatePlanRequest() {
    }
}