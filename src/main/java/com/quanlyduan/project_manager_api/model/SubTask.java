package com.quanlyduan.project_manager_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Entity dai dien cho mot Subtask (Cong viec con) thuoc mot Task (Cong viec cha).
 * Dung de chia nho cac dau viec lon thanh cac checklist co the thuc thi va uoc luong thoi gian.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "sub_tasks")
public class SubTask {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Task cha so huu Subtask nay. Bat buoc phai ton tai Task cha. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id", nullable = false)
    private Task parentTask;

    /** Nguoi truc tiep thuc hien cong viec con nay. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    /** Nguoi dung khoi tao Subtask. Khong cho phep cap nhat lai. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy;

    // ======================================================
    // 3. THONG TIN CO BAN (BASIC INFO)
    // ======================================================
    
    /** Tieu de ngan gon cua cong viec con (vi du: "Viet Unit Test cho API Login"). */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /** Mo ta chi tiet cac buoc thuc hien (neu can). */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ======================================================
    // 4. TRANG THAI & UOC LUONG (STATUS & ESTIMATION)
    // ======================================================
    
    /** * Trang thai hien tai cua Subtask.
     * Gia tri: TO_DO, IN_PROGRESS, DONE. 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubTaskStatus status;

    /** * So gio uoc tinh de hoan thanh cong viec nay. 
     * Ho tro tinh toan tong thoi gian du kien cua Task cha.
     */
    @Column(name = "estimated_hours", precision = 10, scale = 2)
    private BigDecimal estimatedHours;

    /** Thu tu hien thi cua Subtask trong danh sach checklist cua Task cha. */
    @Column(name = "sort_order")
    private Integer sortOrder;

    // ======================================================
    // 5. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public SubTask() {
    }

    public SubTask(Integer id, Task parentTask, User assignee, User createdBy, 
                   String title, String description, SubTaskStatus status, 
                   BigDecimal estimatedHours, Integer sortOrder, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.parentTask = parentTask;
        this.assignee = assignee;
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.status = status;
        this.estimatedHours = estimatedHours;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}