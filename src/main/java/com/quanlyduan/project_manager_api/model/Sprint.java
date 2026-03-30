package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity dai dien cho mot Sprint trong quy trinh Scrum/Agile.
 * Sprint la mot khoang thoi gian co dinh (Time-box) de hoan thanh mot luong 
 * cong viec xac dinh tu Backlog.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "sprints")
public class Sprint {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Du an (Project) so huu Sprint nay. Su dung LAZY fetch de toi uu. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Nguoi dung khoi tao Sprint. Thuong la Scrum Master hoac PM. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy;

    /** Danh sach cac Tasks thuoc ve Sprint nay (Inverse side). */
    @OneToMany(mappedBy = "sprint", fetch = FetchType.LAZY)
    private Set<Task> tasks;

    // ======================================================
    // 3. THONG TIN CO BAN (BASIC INFO)
    // ======================================================
    
    /** Ten hien thi cua Sprint (vi du: "Sprint 01 - MVP Release"). */
    @Column(name = "name", nullable = false)
    private String name;

    /** Ma code dinh danh nhanh (vi du: "PROJ-S1"). */
    @Column(name = "sprint_code", length = 50)
    private String sprintCode;

    /** Muc tieu cot loi can dat duoc sau khi ket thuc Sprint. */
    @Column(name = "goal", columnDefinition = "TEXT")
    private String goal;

    // ======================================================
    // 4. TRANG THAI & DONG THOI GIAN (STATUS & TIMELINE)
    // ======================================================
    
    /** * Trang thai hien tai cua Sprint.
     * Gia tri: NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED. 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SprintStatus status;

    /** Thoi diem bat dau va ket thuc thuc te/du kien. */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    /** Do dai co dinh cua Sprint tinh theo ngay (vi du: 14 ngay cho 2 tuan). */
    @Column(name = "duration_days")
    private Integer durationDays;

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

    public Sprint() {
    }

    public Sprint(Integer id, Project project, User createdBy, Set<Task> tasks, 
                  String name, String sprintCode, String goal, SprintStatus status, 
                  LocalDateTime startDate, LocalDateTime endDate, Integer durationDays, 
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.project = project;
        this.createdBy = createdBy;
        this.tasks = tasks;
        this.name = name;
        this.sprintCode = sprintCode;
        this.goal = goal;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationDays = durationDays;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}