// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateProfileRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.Gender;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    @Size(min = 3, max = 255, message = "Họ và tên phải từ 3 đến 255 ký tự")
    private String fullName; // Đã dịch

    private String avatarUrl; // Đã dịch

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phoneNumber; // Đã dịch

    private LocalDate dateOfBirth; // Đã dịch

    private Gender gender; // Đã dịch
}