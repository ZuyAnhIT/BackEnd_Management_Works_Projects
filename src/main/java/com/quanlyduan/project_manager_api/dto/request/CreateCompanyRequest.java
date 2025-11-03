package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCompanyRequest {

    @NotBlank(message = "Tên công ty không được để trống")
    @Size(min = 3, max = 255, message = "Tên công ty phải từ 3 đến 255 ký tự")
    private String tenCongTy;

    // Các trường khác là tùy chọn
    private String moTa;
    private String diaChi;
    private String soDienThoai;
    private String email;
    private String website;
}
