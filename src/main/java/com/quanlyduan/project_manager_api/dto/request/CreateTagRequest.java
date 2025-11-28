package com.quanlyduan.project_manager_api.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTagRequest {
    @NotBlank(message = "Tên thẻ không được để trống")
    private String name;
    private String color;
    private String description;
}
