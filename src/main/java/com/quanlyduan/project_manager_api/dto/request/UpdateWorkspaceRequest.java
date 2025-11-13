// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateWorkspaceRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UpdateWorkspaceRequest {

    @Size(min = 1, max = 255, message = "Tên không gian làm việc phải từ 1 đến 255 ký tự")
    private String name; // Tên mới
    private String description;
    private String coverImage;
    private String color;
}