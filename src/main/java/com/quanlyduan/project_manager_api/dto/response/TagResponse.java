package com.quanlyduan.project_manager_api.dto.response;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagResponse {
    private Integer id;
    private String name;
    private String color;
    private String description;
    private Integer projectId;
}
