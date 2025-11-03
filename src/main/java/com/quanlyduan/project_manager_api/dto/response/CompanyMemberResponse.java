package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.quanlyduan.project_manager_api.model.common.enums.CombinedMemberStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyMemberResponse {
    
    // Thông tin từ NguoiDung (nếu có)
    private Integer userId;
    private String hoTen;
    private String email;
    private String anhDaiDien;
    
    // Thông tin từ Role
    private String roleName; // Tên vai trò (vd: "Quản trị Công ty")
    
    // Thông tin từ CongTyThanhVien (nếu có)
    private String chucVu;
    private LocalDateTime ngayThamGia;
    
    // Trạng thái kết hợp
    private CombinedMemberStatus status;
}