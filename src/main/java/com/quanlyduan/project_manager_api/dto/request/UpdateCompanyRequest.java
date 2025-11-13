// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateCompanyRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCompanyRequest {

    // Admin có thể chỉ gửi 1 trong các trường này, không bắt buộc tất cả
    
    @Size(min = 3, max = 255, message = "Tên công ty phải có độ dài từ 3 đến 255 ký tự") // Đã dịch
    private String companyName; // Đã dịch

    private String description; // Đã dịch
    private String logo;
    private String address; // Đã dịch
    private String phoneNumber; // Đã dịch
    private String email;
    private String website;
}