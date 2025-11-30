// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateCompanyRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật thông tin Công ty.
 * Hỗ trợ cập nhật từng phần (Partial Update):
 * - Nếu client gửi giá trị null cho một trường, hệ thống sẽ giữ nguyên giá trị cũ trong Database.
 * - Nếu client gửi giá trị mới, hệ thống sẽ cập nhật.
 */
@Data
public class UpdateCompanyRequest {

    // Tên công ty mới (Tùy chọn, nhưng nếu có giá trị thì phải đúng độ dài)
    @Size(min = 3, max = 255, message = "Company name must be between 3 and 255 characters")
    private String companyName;

    // Mô tả mới (Tùy chọn)
    private String description;

    // Đường dẫn Logo mới (Tùy chọn - thường là URL string hoặc xử lý qua multipart riêng)
    private String logo;

    // Địa chỉ trụ sở mới (Tùy chọn)
    private String address;

    // Số điện thoại liên hệ mới (Tùy chọn)
    private String phoneNumber;

    // Email liên hệ mới (Tùy chọn)
    private String email;

    // Website mới (Tùy chọn)
    private String website;
}