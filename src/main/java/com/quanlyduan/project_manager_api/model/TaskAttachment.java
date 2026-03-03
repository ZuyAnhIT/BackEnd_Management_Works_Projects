package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

// JPA & Hibernate
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity lưu trữ thông tin về một Tệp đính kèm (Attachment) cho Task.
 */
@Entity
@Table(name = "task_attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachment {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    // ==========================================
    // RELATIONSHIPS (Quan hệ Entity)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task; // Task mà tệp này được đính kèm vào

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false, updatable = false)
    private User uploadedBy; // Người dùng đã tải tệp lên

    // ==========================================
    // FILE INFORMATION (Thông tin tệp)
    // ==========================================
    @Column(name = "file_name", nullable = false)
    private String fileName; // Tên gốc của tệp

    @Column(name = "file_path", nullable = false)
    private String filePath; // Đường dẫn vật lý (local) hoặc key trên dịch vụ lưu trữ (S3/MinIO)

    @Column(name = "file_type")
    private String fileType; // Loại tệp (MIME Type), ví dụ: "image/png"

    @Column(name = "file_size")
    private Long fileSize; // Kích thước tệp (theo bytes)

    // ==========================================
    // TIMESTAMPS (Thời gian hệ thống)
    // ==========================================
    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt; // Thời điểm tệp được tải lên

}