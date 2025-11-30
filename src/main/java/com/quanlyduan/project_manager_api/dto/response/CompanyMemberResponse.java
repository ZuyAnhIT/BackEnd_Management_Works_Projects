// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/CompanyMemberResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.CombinedMemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin chi tiết của một Thành viên trong Công ty.
 * DTO này được sử dụng chung cho cả:
 * 1. Thành viên chính thức (User đã có trong bảng company_members).
 * 2. Lời mời đang chờ (User chưa chấp nhận, lấy từ bảng company_invitations).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyMemberResponse {
    
    // ========================================================================
    // 1. THÔNG TIN ĐỊNH DANH (IDENTITY)
    // ========================================================================

    // ID định danh của bản ghi trong bảng 'company_members'.
    // Lưu ý: Sẽ là NULL nếu đây là một lời mời đang chờ (Pending Invitation) chưa được chấp nhận.
    private Integer memberId; 

    // ID của tài khoản người dùng (User ID).
    // Lưu ý: Sẽ là NULL nếu người được mời chưa có tài khoản trong hệ thống.
    private Integer userId;

    // ========================================================================
    // 2. THÔNG TIN CÁ NHÂN (PERSONAL INFO)
    // ========================================================================

    // Họ và tên hiển thị.
    private String fullName; 

    // Địa chỉ Email liên hệ.
    private String email;

    // Số điện thoại liên hệ.
    private String phoneNumber;

    // Đường dẫn ảnh đại diện (Avatar).
    private String avatarUrl; 
    
    // ========================================================================
    // 3. THÔNG TIN CÔNG VIỆC & VAI TRÒ (WORK & ROLE INFO)
    // ========================================================================

    // Tên vai trò trong công ty (ví dụ: "Quản trị viên", "Thành viên").
    private String roleName; 
    
    // Chức danh công việc cụ thể (ví dụ: "Frontend Dev", "HR Manager").
    private String jobTitle; 

    // Thời điểm tham gia công ty (hoặc thời điểm gửi lời mời).
    private LocalDateTime joinedAt; 
    
    // ========================================================================
    // 4. TRẠNG THÁI (STATUS)
    // ========================================================================

    // Trạng thái tổng hợp của thành viên.
    // Bao gồm: ACTIVE (Hoạt động), SUSPENDED (Tạm dừng), REMOVED (Đã xóa), PENDING (Đang chờ).
    private CombinedMemberStatus status;
}