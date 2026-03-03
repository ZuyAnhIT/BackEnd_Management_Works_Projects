package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi người dùng tạo một Công ty (Company) mới.
 */
@Data
public class CreateCompanyRequest {

    // ==========================================
    // REQUEST DATA (Thông tin công ty)
    // ==========================================

    /**
     * Tên công ty.
     * Bắt buộc phải có, độ dài từ 3 đến 255 ký tự.
     */
    @NotBlank(message = "Company name must not be blank")
    @Size(min = 3, max = 255, message = "Company name must be between 3 and 255 characters")
    private String companyName;

    /**
     * Mô tả tổng quan về công ty.
     * (Tùy chọn)
     */
    private String description;

    /**
     * Địa chỉ trụ sở chính của công ty.
     * (Tùy chọn)
     */
    private String address;

    /**
     * Số điện thoại liên hệ của công ty.
     * (Tùy chọn)
     */
    private String phoneNumber;

    /**
     * Email liên hệ chung của công ty.
     * (Tùy chọn)
     */
    private String email;

    /**
     * Đường dẫn Website của công ty.
     * (Tùy chọn)
     */
    private String website;

}