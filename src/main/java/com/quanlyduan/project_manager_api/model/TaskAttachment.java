package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

// Import các model cần thiết
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.Task;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_attachments") // Tên bảng lưu trữ
public class TaskAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "file_name", nullable = false)
    private String fileName; // Tên gốc của tệp

    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl; // Đường dẫn (URL) để truy cập tệp

    @Column(name = "file_type")
    private String fileType; // Kiểu tệp (ví dụ: "image/png")

    @Column(name = "file_size")
    private Long fileSize; // Kích thước tệp (tính bằng bytes)

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt; // Thời điểm tải lên

    // Mối quan hệ: Ai là người tải tệp này lên
    // Service sẽ dùng 'uploaderId' để set đối tượng User này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Mối quan hệ: Tệp này thuộc về Task nào
    // Service sẽ dùng 'taskId' để set đối tượng Task này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}