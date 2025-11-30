// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/CompanyInvitationResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin chi tiết của một Lời mời tham gia Công ty.
 * Thường được sử dụng trong danh sách quản lý lời mời để Admin theo dõi trạng thái.
 */
@Data
@Builder
public class CompanyInvitationResponse {

    // ID định danh duy nhất của lời mời
    private Integer id;

    // Địa chỉ Email của người được mời
    private String email;

    // Tên vai trò dự kiến sẽ được gán sau khi chấp nhận (ví dụ: "COMPANY_MEMBER")
    private String roleName;

    // Tên hiển thị của người đã gửi lời mời (thường là Admin)
    private String invitedByName;

    // Trạng thái hiện tại của lời mời (ví dụ: "PENDING", "EXPIRED")
    private String status;

    // Thời gian hết hạn của lời mời
    private LocalDateTime expiresAt;

    // Đường dẫn URL đầy đủ để chấp nhận lời mời.
    // Hữu ích để Admin có thể copy link và gửi thủ công qua kênh khác (Chat, Zalo...) nếu cần.
    private String invitationLink;
}