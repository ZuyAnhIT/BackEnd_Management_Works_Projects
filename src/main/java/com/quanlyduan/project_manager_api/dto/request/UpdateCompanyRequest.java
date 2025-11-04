package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCompanyRequest {

    // Admin có thể chỉ gửi 1 trong các trường này, không bắt buộc tất cả
    
    @Size(min = 3, max = 255, message = "Tên công ty phải từ 3 đến 255 ký tự")
    private String tenCongTy;

    private String moTa;
    private String logo;
    private String diaChi;
    private String soDienThoai;
    private String email;
    private String website;
}
