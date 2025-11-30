// File: src/main/java/com/quanlyduan/project_manager_api/model/TaskComment.java
package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_comments") 
/**
 * Entity lưu trữ Bình luận (Comment) của Task.
 */
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // Nội dung bình luận

    // Người bình luận (Commenter)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commenter_id", nullable = false)
    private User user; // Người dùng đã bình luận

    // Task liên quan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo bình luận

    
}