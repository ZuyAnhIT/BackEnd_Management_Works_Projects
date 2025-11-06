// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/CompanyDetailsResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDetailsResponse {
    private Integer companyId; // Đã dịch
    private String companyName; // Đã dịch
    private String companyCode; // Đã dịch
    private String description; // Đã dịch
    private String logo;
    private String address; // Đã dịch
    private String phoneNumber; // Đã dịch
    private String email;
    private String website;
    private Integer createdById; // Có thể cần để biết ai là owner // Đã dịch
}