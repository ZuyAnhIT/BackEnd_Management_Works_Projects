package com.quanlyduan.project_manager_api.dto.request;
import lombok.Data;

@Data
public class UpdateTagRequest {
    private String name;
    private String color;
    private String description;
}
