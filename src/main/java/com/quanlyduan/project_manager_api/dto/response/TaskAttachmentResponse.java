// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskAttachmentResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin chi tiết của một Tệp đính kèm (Attachment) trong Task.
 * Thường được sử dụng để hiển thị danh sách file trong chi tiết công việc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachmentResponse {

    // ========================================================================
    // 1. THÔNG TIN ĐỊNH DANH & FILE (FILE INFO)
    // ========================================================================

    // ID định danh của tệp đính kèm
    private Integer id;

    // ID của Task chứa tệp này
    private Integer taskId;

    // Tên gốc của file (ví dụ: "design_mockup.png")
    private String fileName;

    // Loại file (MIME type, ví dụ: "image/png", "application/pdf")
    private String fileType;

    // Kích thước file (tính bằng bytes)
    // Frontend có thể dùng để hiển thị (ví dụ: 2.5 MB)
    private Long fileSize;

    // Đường dẫn URL để tải hoặc xem file.
    // Ví dụ: "/api/files/uuid-abc.png" hoặc link S3 đầy đủ.
    private String fileUrl;

    // ========================================================================
    // 2. THÔNG TIN NGƯỜI TẢI LÊN (UPLOADER INFO)
    // ========================================================================

    // ID của người đã tải file lên
    private Integer uploadedById;

    // Tên hiển thị của người tải file
    private String uploadedByName;

    // Thời điểm file được tải lên
    private LocalDateTime uploadedAt;
}