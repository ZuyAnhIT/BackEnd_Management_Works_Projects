// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/CompanyMembershipDTO.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

// DTO con: Chứa thông tin 1 vai trò trong 1 công ty
@Data
@AllArgsConstructor
public class CompanyMembershipDTO {
    private Integer companyId;
    private String companyName; // Đã dịch
    private String roleCode; // (vd: "COMPANY_ADMIN" hoặc "COMPANY_MEMBER")
}