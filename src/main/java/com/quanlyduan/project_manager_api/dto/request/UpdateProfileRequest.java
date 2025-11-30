// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateProfileRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.Gender;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật thông tin cá nhân (Profile) của người dùng.
 * Hỗ trợ cập nhật từng phần (Partial Update):
 * - Chỉ những trường có giá trị (không null) mới được cập nhật vào hệ thống.
 */
@Data
public class UpdateProfileRequest {

    // Họ và tên hiển thị mới (Tùy chọn, độ dài từ 3 đến 255 ký tự)
    @Size(min = 3, max = 255, message = "Full name must be between 3 and 255 characters")
    private String fullName;

    // Đường dẫn ảnh đại diện mới (Tùy chọn - thường được cập nhật tự động khi upload file)
    private String avatarUrl;

    // Số điện thoại liên hệ mới (Tùy chọn, tối đa 20 ký tự)
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    // Ngày sinh mới (Tùy chọn)
    private LocalDate dateOfBirth;

    // Giới tính mới (Enum: MALE, FEMALE, OTHER - Tùy chọn)
    private Gender gender;
}