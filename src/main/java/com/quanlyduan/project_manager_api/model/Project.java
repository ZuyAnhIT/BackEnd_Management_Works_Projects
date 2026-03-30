package com.quanlyduan.project_manager_api.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.ProjectPriority;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;

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
 * Entity dai dien cho mot Du an (Project).
 * Day la don vi quan ly thuc thi chinh, chua dung cac Tasks, Sprints va Epics 
 * thuoc mot Workspace cu the.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "projects")
public class Project {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Workspace chu quan cua du an nay. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    /** Phan loai linh vuc du an (vi du: Software, Marketing). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_type_id")
    private ProjectType projectType;

    /** Nguoi chiu trach nhiem chinh dieu hanh du an (Project Manager). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    /** Nguoi dung khoi tao ban ghi du an. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy;

    // ======================================================
    // 3. THONG TIN CO BAN (BASIC INFO)
    // ======================================================
    
    /** Ten hien thi cua du an. */
    @Column(name = "name", nullable = false)
    private String name;

    /** * Ma code viet tat cua du an (vi du: "WEB", "CRM"). 
     * Dung lam tien to (Prefix) de sinh ma Task tu dong.
     */
    @Column(name = "project_code", nullable = false)
    private String projectCode;

    /** Mo ta chi tiet ve pham vi va muc tieu chien luoc. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "goal", columnDefinition = "TEXT")
    private String goal;

    // ======================================================
    // 4. TRANG THAI & DO UU TIEN (STATUS & PRIORITY)
    // ======================================================
    
    /** Trang thai van hanh (vi du: NEW, IN_PROGRESS, COMPLETED). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status;

    /** Muc do quan trong (vi du: LOW, MEDIUM, HIGH). */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private ProjectPriority priority;

    // ======================================================
    // 5. DONG THOI GIAN & TIEN ĐO (TIMELINE & METRICS)
    // ======================================================
    
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    /** Thoi diem du an thuc te duoc danh dau la hoan thanh. */
    @Column(name = "completed_at")
    private LocalDate completedAt;

    /** * Tien do hoan thanh du an (Scale 0.00 - 1.00 hoac 0 - 100).
     * Thuong duoc tinh toan dua tren so luong Task hoan thanh.
     */
    @Column(name = "progress")
    private BigDecimal progress;

    // ======================================================
    // 6. GIAO DIEN & CAU HINH (UI & CONFIG)
    // ======================================================
    
    /** Anh bia cua du an de render tren giao dien Dashboard. */
    @Column(name = "cover_image_url")
    private String coverImageUrl;

    /** * Cau hinh bo cuc bang (Kanban/Scrum) luu duoi dang JSON.
     * Cho phep tuy chinh cot, filter mac dinh cho tung du an.
     */
    @Column(name = "board_config", columnDefinition = "JSON")
    private String boardConfig;

    // ======================================================
    // 7. THONG TIN HE THONG (AUDIT INFO)
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

    public Project() {
    }

    public Project(Integer id, Workspace workspace, ProjectType projectType, User manager, 
                   User createdBy, String name, String projectCode, String description, 
                   String goal, ProjectStatus status, ProjectPriority priority, 
                   LocalDate startDate, LocalDate dueDate, LocalDate completedAt, 
                   BigDecimal progress, String coverImageUrl, String boardConfig, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.workspace = workspace;
        this.projectType = projectType;
        this.manager = manager;
        this.createdBy = createdBy;
        this.name = name;
        this.projectCode = projectCode;
        this.description = description;
        this.goal = goal;
        this.status = status;
        this.priority = priority;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.completedAt = completedAt;
        this.progress = progress;
        this.coverImageUrl = coverImageUrl;
        this.boardConfig = boardConfig;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}