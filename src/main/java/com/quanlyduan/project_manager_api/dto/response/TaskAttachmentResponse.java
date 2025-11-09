// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskAttachmentResponse.java
// DTO CHO USER STORY 11
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskAttachmentResponse {
    private Integer id;
    private Integer taskId;
    private String fileName;
    private String fileType;
    private Long fileSize; // Kích thước (bytes)
    private String fileUrl; // URL để tải về (quan trọng)
    private Integer uploadedById;
    private String uploadedByName; // Bổ sung
    private LocalDateTime uploadedAt;
}
