// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/CompanyMembershipDTO.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con (Nested DTO) chứa thông tin về tư cách thành viên của người dùng trong một Công ty cụ thể.
 * Thường được sử dụng trong danh sách "My Companies" hoặc thông tin Profile mở rộng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyMembershipDTO {

    // ID định danh của công ty
    private Integer companyId;

    // Tên hiển thị của công ty
    private String companyName; 

    // Mã vai trò của người dùng trong công ty này.
    // Ví dụ: "COMPANY_ADMIN" (Quản trị viên) hoặc "COMPANY_MEMBER" (Thành viên).
    // Frontend dùng trường này để quyết định có hiển thị nút "Cài đặt công ty" hay không.
    private String roleCode; 

    private String companyStatus;
}