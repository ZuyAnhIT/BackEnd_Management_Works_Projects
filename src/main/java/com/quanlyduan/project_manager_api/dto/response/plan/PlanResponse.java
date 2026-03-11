package com.quanlyduan.project_manager_api.dto.response.plan;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PlanResponse {
    private Integer id;
    private String planCode;
    private String name;
    private String description;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer maxUsers;
    private Integer maxWorkspaces;
    private Integer maxProjects;
    private Integer maxStorageGb;
    private JsonNode features;
    private Boolean isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}