package com.quanlyduan.project_manager_api.dto.response;

// Import DTO con từ file TaskCommentResponse để tái sử dụng
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse.CommentUserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachmentResponse {

    private Integer id; // ID của tệp đính kèm
    private String fileName; // Tên file gốc
    private String fileUrl; // Đường dẫn (URL) để tải file
    private String fileType; // Kiểu file (ví dụ: "image/png")
    private Long fileSize; // Kích thước file (bytes)
    private LocalDateTime uploadedAt; // Thời điểm tải lên

    // Thông tin cơ bản của người tải tệp lên
    // Tái sử dụng DTO 'CommentUserResponse' từ file mẫu của bạn
    private CommentUserResponse uploader;
}