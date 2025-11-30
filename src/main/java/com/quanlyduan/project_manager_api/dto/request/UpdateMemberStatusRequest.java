// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateMemberStatusRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật trạng thái hoạt động của thành viên.
 * Được sử dụng để Tạm dừng (Suspend), Kích hoạt lại (Activate) hoặc Khôi phục (Restore) thành viên.
 * Lưu ý: Không dùng để Xóa (Remove), xóa có API riêng.
 */
@Data
public class UpdateMemberStatusRequest {

    // Trạng thái mới muốn áp dụng cho thành viên (Bắt buộc)
    // Các giá trị hợp lệ thường là: ACTIVE, SUSPENDED
    // Giá trị REMOVED thường bị chặn ở tầng Service vì cần dùng API xóa riêng.
    @NotNull(message = "New status must not be null")
    private MemberStatus newStatus; 
}