package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity luu tru Binh luan (Comment) cua Cong viec.
 * Ho tro trao doi thong tin, cap nhat tien do va luu lai lich su thao luan giua cac thanh vien.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "task_comments") 
public class TaskComment {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Nguoi dung thuc hien viet binh luan. Su dung LAZY fetch de toi uu. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commenter_id", nullable = false)
    private User user;

    /** Cong viec (Task) chua binh luan nay. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // ======================================================
    // 3. NOI DUNG BINH LUAN (CONTENT)
    // ======================================================
    
    /** Noi dung chi tiet cua binh luan (ho tro van ban dai). */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // ======================================================
    // 4. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    /** Thoi diem binh luan duoc tao (tu dong sinh boi Hibernate). */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay cho Hibernate.
     */
    public TaskComment() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong minh bach.
     */
    public TaskComment(Integer id, User user, Task task, String content, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.task = task;
        this.content = content;
        this.createdAt = createdAt;
    }
}