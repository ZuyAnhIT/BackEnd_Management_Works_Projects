package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.model.CongTy;
import com.quanlyduan.project_manager_api.model.CongTyLoiMoi;
import com.quanlyduan.project_manager_api.model.NguoiDung;
import com.quanlyduan.project_manager_api.model.Role;

/**
 * Service này chứa logic chung cho việc xử lý lời mời,
 * được sử dụng bởi cả AuthServiceImpl và CompanyServiceImpl.
 */
public interface InvitationService {

    /**
     * Xác thực một token lời mời.
     * Kiểm tra xem token có tồn tại, còn hạn, và đang PENDING hay không.
     *
     * @param token Chuỗi token từ URL
     * @return Đối tượng CongTyLoiMoi nếu hợp lệ
     * @throws ResourceNotFoundException nếu token không tồn tại
     * @throws BadRequestException nếu token đã hết hạn hoặc đã được sử dụng
     */
    CongTyLoiMoi validateInvitationToken(String token);

    /**
     * Thêm một người dùng vào bảng CongTyThanhVien.
     *
     * @param user Người dùng (mới hoặc cũ)
     * @param congTy Công ty để tham gia
     * @param role Role được gán
     */
    void addMemberToCompany(NguoiDung user, CongTy congTy, Role role);
}
