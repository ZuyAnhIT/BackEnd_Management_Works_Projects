package com.quanlyduan.project_manager_api.dto.request.plan;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePlanRequest {

    // Không dùng @NotBlank nữa, cho phép null
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Plan code must contain only uppercase letters, numbers, and underscores")
    private String planCode;

    private String name;

    private String description;

    @Min(value = 0, message = "Monthly price cannot be negative")
    private BigDecimal monthlyPrice;

    @Min(value = 0, message = "Yearly price cannot be negative")
    private BigDecimal yearlyPrice;

    @Min(value = -1, message = "Max users must be -1 or greater")
    private Integer maxUsers;

    @Min(value = -1, message = "Max workspaces must be -1 or greater")
    private Integer maxWorkspaces;

    @Min(value = -1, message = "Max projects must be -1 or greater")
    private Integer maxProjects;

    @Min(value = -1, message = "Max storage must be -1 or greater")
    private Integer maxStorageGb;

    private JsonNode features;

    private Boolean isActive;

    private Integer sortOrder;
}