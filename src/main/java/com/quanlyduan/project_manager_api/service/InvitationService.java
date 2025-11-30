// File: src/main/java/com/quanlyduan/project_manager_api/service/InvitationService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyInvitation;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.Role;

/**
 * Interface Service chứa logic chung cho việc xử lý lời mời,
 * được sử dụng bởi cả AuthServiceImpl và CompanyServiceImpl.
 */
public interface InvitationService {

    // ========================================================================
    // 1. XÁC THỰC VÀ KIỂM TRA (VALIDATION)
    // ========================================================================

    /**
     * Xác thực một token lời mời tham gia Công ty.
     * Kiểm tra xem token có tồn tại, còn hạn, và đang ở trạng thái PENDING hay không.
     *
     * @param token Chuỗi token từ URL.
     * @return Đối tượng CompanyInvitation nếu hợp lệ.
     * @throws ResourceNotFoundException nếu token không tồn tại.
     * @throws BadRequestException nếu token đã hết hạn hoặc đã được sử dụng.
     */
    CompanyInvitation validateInvitationToken(String token);

    // ========================================================================
    // 2. HÀNH ĐỘNG (ACTION)
    // ========================================================================

    /**
     * Thêm một người dùng (mới hoặc cũ) vào bảng CompanyMember.
     *
     * @param user Người dùng.
     * @param company Công ty để tham gia.
     * @param role Role được gán.
     */
    void addMemberToCompany(User user, Company company, Role role);
}