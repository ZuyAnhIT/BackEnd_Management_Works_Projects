// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateCompanyRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCompanyRequest {

    @NotBlank(message = "Company name must not be blank") // Đã dịch
    @Size(min = 3, max = 255, message = "Company name must be between 3 and 255 characters") // Đã dịch
    private String companyName; // Đã dịch

    // Các trường khác là tùy chọn
    private String description; // Đã dịch
    private String address; // Đã dịch
    private String phoneNumber; // Đã dịch
    private String email;
    private String website;
}