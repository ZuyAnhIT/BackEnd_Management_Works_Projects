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
 * Entity lưu trữ Bình luận (Comment) của Task.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_comments") 
public class TaskComment {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    // ==========================================
    // RELATIONSHIPS (Quan hệ Entity)
    // ==========================================
    // Người bình luận (Commenter)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commenter_id", nullable = false)
    private User user; // Người dùng đã bình luận

    // Task liên quan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // ==========================================
    // COMMENT CONTENT (Nội dung)
    // ==========================================
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // Nội dung bình luận

    // ==========================================
    // TIMESTAMPS (Thời gian hệ thống)
    // ==========================================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo bình luận

}