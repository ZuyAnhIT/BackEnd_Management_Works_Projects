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
    private Integer companyId;
    private String companyName;
    private String companyCode;
    private String description;
    private String logo;
    private String address;
    private String phoneNumber;
    private String email;
    private String website;
}
