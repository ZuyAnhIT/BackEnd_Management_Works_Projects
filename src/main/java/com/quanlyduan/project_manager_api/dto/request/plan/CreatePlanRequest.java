package com.quanlyduan.project_manager_api.dto.request.plan;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu Client de tao moi mot Goi cuoc (Subscription Plan).
 */
@Getter
@Setter
public class CreatePlanRequest {

    // ======================================================
    // KHAI BAO HANG SO
    // ======================================================
    public static final String PLAN_CODE_REGEX = "^[A-Z0-9_]+$";

    // ======================================================
    // 1. THONG TIN CO BAN (IDENTIFICATION)
    // ======================================================

    @NotBlank(message = "Plan code must not be blank")
    @Pattern(regexp = PLAN_CODE_REGEX, message = "Plan code must contain only uppercase letters, numbers, and underscores")
    private String planCode;

    @NotBlank(message = "Plan name must not be blank")
    private String name;

    private String description;

    // ======================================================
    // 2. CAU HINH GIA CA (PRICING)
    // ======================================================

    @NotNull(message = "Monthly price is required")
    @Min(value = 0, message = "Monthly price cannot be negative")
    private BigDecimal monthlyPrice;

    @NotNull(message = "Yearly price is required")
    @Min(value = 0, message = "Yearly price cannot be negative")
    private BigDecimal yearlyPrice;

    // ======================================================
    // 3. GIOI HAN TAI NGUYEN (QUOTA/LIMITS)
    // Ghi chu: Gia tri -1 dai dien cho viec khong gioi han (Unlimited)
    // ======================================================

    @NotNull(message = "Max users limit is required. Use -1 for unlimited.")
    @Min(value = -1, message = "Max users must be -1 or greater")
    private Integer maxUsers;

    @NotNull(message = "Max workspaces limit is required. Use -1 for unlimited.")
    @Min(value = -1, message = "Max workspaces must be -1 or greater")
    private Integer maxWorkspaces;

    @NotNull(message = "Max projects limit is required. Use -1 for unlimited.")
    @Min(value = -1, message = "Max projects must be -1 or greater")
    private Integer maxProjects;

    @NotNull(message = "Max storage limit is required. Use -1 for unlimited.")
    @Min(value = -1, message = "Max storage must be -1 or greater")
    private Integer maxStorageGb;

    // ======================================================
    // 4. TINH NANG VA TRANG THAI (FEATURES & SETTINGS)
    // ======================================================

    // Danh sach cac tinh nang duoc Frontend gui len duoi dang JSON Object
    private JsonNode features;

    @NotNull(message = "Status (isActive) is required")
    private Boolean isActive;

    // Thu tu hien thi goi cuoc tren giao dien bang gia
    private Integer sortOrder;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh ho tro Jackson Deserialize chuoi JSON thanh Object.
     */
    public CreatePlanRequest() {
    }
}