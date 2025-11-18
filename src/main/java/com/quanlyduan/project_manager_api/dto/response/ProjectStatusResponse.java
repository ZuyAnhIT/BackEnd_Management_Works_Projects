// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectStatusResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectStatusResponse {
    private Integer id;
    private Integer projectId;
    private String name;
    private String color;
    private Integer sortOrder;
    private Boolean isCompletedStatus;
}