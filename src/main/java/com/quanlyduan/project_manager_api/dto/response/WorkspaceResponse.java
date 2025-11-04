package com.quanlyduan.project_manager_api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {
    private Integer idKhongGian;
    private Integer congTyId;
    private String tenKhongGian;
    private String moTa;
    private String anhBia;
    private String mauSac;
    private Integer nguoiTaoId;
    private String trangThai; 
    private LocalDateTime ngayTao;
}