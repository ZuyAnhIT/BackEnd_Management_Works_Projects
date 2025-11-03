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
    private Integer idCongTy;
    private String tenCongTy;
    private String maCongTy;
    private String moTa;
    private String logo;
    private String diaChi;
    private String soDienThoai;
    private String email;
    private String website;
    private Integer nguoiTaoId; // Có thể cần để biết ai là owner
}