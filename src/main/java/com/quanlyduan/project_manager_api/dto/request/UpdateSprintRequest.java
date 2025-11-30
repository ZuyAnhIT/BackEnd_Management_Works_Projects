// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateSprintRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật thông tin Sprint.
 * Hỗ trợ cập nhật từng phần (Partial Update):
 * - Các trường là tùy chọn (Optional).
 * - Nếu client gửi giá trị null, hệ thống sẽ giữ nguyên giá trị cũ trong Database.
 */
@Data
public class UpdateSprintRequest {

    // Tên mới của Sprint (Tùy chọn, độ dài từ 1-255 ký tự)
    @Size(min = 1, max = 255, message = "Sprint name must be between 1 and 255 characters")
    private String name;

    // Mục tiêu của Sprint mới (Tùy chọn)
    private String goal;

    // Thời gian bắt đầu mới (Tùy chọn, bao gồm cả giờ phút)
    // Định dạng chuẩn ISO: YYYY-MM-DDTHH:mm:ss
    private LocalDateTime startDate;

    // Thời gian kết thúc mới (Tùy chọn, bao gồm cả giờ phút)
    private LocalDateTime endDate;
}