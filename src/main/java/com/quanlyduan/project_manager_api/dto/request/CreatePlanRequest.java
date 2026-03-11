package com.quanlyduan.project_manager_api.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePlanRequest {

    @NotBlank(message = "Plan code must not be blank")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Plan code must contain only uppercase letters, numbers, and underscores")
    private String planCode;

    @NotBlank(message = "Plan name must not be blank")
    private String name;

    private String description;

    @NotNull(message = "Monthly price is required")
    @Min(value = 0, message = "Monthly price cannot be negative")
    private BigDecimal monthlyPrice;

    @NotNull(message = "Yearly price is required")
    @Min(value = 0, message = "Yearly price cannot be negative")
    private BigDecimal yearlyPrice;

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

    // Frontend sẽ gửi một Object JSON chứa danh sách tính năng
    private JsonNode features;

    @NotNull(message = "Status (isActive) is required")
    private Boolean isActive;

    private Integer sortOrder;
}