// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/CompanyDetailsResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi thông tin chi tiết của một Công ty.
 * Được sử dụng khi xem chi tiết hoặc cập nhật thông tin công ty.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDetailsResponse {
    
    // ID định danh của công ty trong CSDL
    private Integer companyId;

    // Tên hiển thị của công ty
    private String companyName;

    // Mã định danh duy nhất (ví dụ: "TECH", "ABC")
    private String companyCode;

    // Mô tả giới thiệu về công ty
    private String description;

    // Đường dẫn URL đến logo của công ty (đã upload)
    private String logo;

    // Địa chỉ trụ sở
    private String address;

    // Số điện thoại liên hệ
    private String phoneNumber;

    // Email liên hệ chung
    private String email;

    // Website chính thức
    private String website;
}