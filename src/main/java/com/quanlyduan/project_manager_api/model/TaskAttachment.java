// File: src/main/java/com/quanlyduan/project_manager_api/model/TaskAttachment.java
package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Entity lưu trữ thông tin về một Tệp đính kèm (Attachment) cho Task.
 */
public class TaskAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task; // Task mà tệp này được đính kèm vào

    @Column(name = "file_name", nullable = false)
    private String fileName; // Tên gốc của tệp

    @Column(name = "file_path", nullable = false)
    private String filePath; // Đường dẫn vật lý (local) hoặc key trên dịch vụ lưu trữ (S3/MinIO)

    @Column(name = "file_type")
    private String fileType; // Loại tệp (MIME Type), ví dụ: "image/png"

    @Column(name = "file_size")
    private Long fileSize; // Kích thước tệp (theo bytes)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false, updatable = false)
    private User uploadedBy; // Người dùng đã tải tệp lên

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt; // Thời điểm tệp được tải lên
}