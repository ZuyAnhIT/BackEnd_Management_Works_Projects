package com.quanlyduan.project_manager_api.dto.response.plan;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO hiển thị Gói cước dành riêng cho Khách hàng (Public/Client).
 * Đã loại bỏ toàn bộ các trường nhạy cảm của hệ thống.
 */
@Data
@Builder
public class PublicPlanResponse {
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
    
}