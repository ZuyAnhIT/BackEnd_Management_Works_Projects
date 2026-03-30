package com.quanlyduan.project_manager_api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;

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
 * Entity dai dien cho mot Epic - Khoi luong cong viec lon trong mo hinh Agile.
 * Epic gom nhom nhieu Tasks/User Stories lien quan de theo doi tien do chien luoc cua Project.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "epics")
public class Epic {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Du an (Project) so huu Epic nay. Su dung LAZY fetch de toi uu. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Nguoi dung khoi tao Epic. Khong cho phep cap nhat lai nguoi tao. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy;

    /** Danh sach cac Tasks thuoc ve Epic nay (Inverse side). */
    @OneToMany(mappedBy = "epic", fetch = FetchType.LAZY)
    private List<Task> tasks;

    // ======================================================
    // 3. THONG TIN CO BAN (BASIC INFO)
    // ======================================================
    
    /** Ten hien thi cua Epic (vi du: "He thong Thanh toan v2"). */
    @Column(name = "name", nullable = false)
    private String name;

    /** Ma code dinh danh Epic (vi du: "PROJ-E-1"). */
    @Column(name = "epic_code", length = 50)
    private String epicCode;

    /** Mo ta chi tiet ve muc tieu va pham vi cua Epic. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Ma mau HEX dung de phan biet Epic tren giao dien (vi du: "#FF5733"). */
    @Column(name = "color", length = 7)
    private String color;

    /** * Trang thai hien tai cua Epic.
     * Gia tri: OPEN (Dang mo), IN_PROGRESS (Dang lam), DONE (Hoan tat), CANCELLED. 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EpicStatus status;

    // ======================================================
    // 4. DONG THOI GIAN (TIMELINE)
    // ======================================================
    
    /** Ngay bat dau va Ngay het han du kien cua toan bo Epic. */
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

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

    public Epic() {
    }

    public Epic(Integer id, Project project, User createdBy, List<Task> tasks, 
                String name, String epicCode, String description, String color, 
                EpicStatus status, LocalDate startDate, LocalDate dueDate, 
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.project = project;
        this.createdBy = createdBy;
        this.tasks = tasks;
        this.name = name;
        this.epicCode = epicCode;
        this.description = description;
        this.color = color;
        this.status = status;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}