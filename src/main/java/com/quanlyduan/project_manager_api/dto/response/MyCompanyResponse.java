package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO hiển thị thông tin một công ty mà người dùng đang là thành viên.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCompanyResponse {

    private Integer companyId;
    private String companyName;
    private String companyCode;
    private String description;
    private String logoUrl;
    private String roleCode;
    private String memberStatus;
    private String jobTitle;
    private String department;
    private LocalDateTime joinedAt;
}
