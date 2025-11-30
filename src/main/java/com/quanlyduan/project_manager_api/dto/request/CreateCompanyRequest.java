// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateCompanyRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO nhận dữ liệu khi người dùng tạo một Công ty mới.
 */
@Data
public class CreateCompanyRequest {

    // Tên công ty (Bắt buộc, độ dài từ 3-255 ký tự)
    @NotBlank(message = "Company name must not be blank")
    @Size(min = 3, max = 255, message = "Company name must be between 3 and 255 characters")
    private String companyName;

    // Mô tả công ty (Tùy chọn)
    private String description;

    // Địa chỉ trụ sở (Tùy chọn)
    private String address;

    // Số điện thoại liên hệ (Tùy chọn)
    private String phoneNumber;

    // Email liên hệ chung (Tùy chọn)
    private String email;

    // Website công ty (Tùy chọn)
    private String website;
}