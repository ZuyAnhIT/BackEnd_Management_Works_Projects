package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWorkspaceRequest {

    @NotBlank(message = "Tên không gian không được để trống")
    private String tenKhongGian;

    private String moTa;
    private String anhBia;
    private String mauSac; // (vd: #3498db)
}
