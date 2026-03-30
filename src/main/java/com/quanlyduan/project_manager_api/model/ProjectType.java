package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.ProjectModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity dai dien cho Loai Du an (Project Type) - Khuon mau cau hinh cho cac du an moi.
 * Xac dinh mo hinh quan ly (Scrum, Kanban) va cac thiet lap dac thu cho tung linh vuc.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "project_types")
public class ProjectType {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. THONG TIN CO BAN (BASIC INFO)
    // ======================================================
    
    /** Ten hien thi cua loai du an (vi du: "Phat trien Phan mem Scrum", "Marketing Campaign"). */
    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    /** Ma code duy nhat dung de truy van nhanh (vi du: "SW_SCRUM", "MK_KANBAN"). */
    @Column(name = "type_code", unique = true, length = 50)
    private String typeCode;

    /** Mo ta chi tiet ve cach thuc van hanh hoac doi tuong ap dung cua loai du an nay. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ======================================================
    // 3. MO HINH & CAU HINH (MODEL & CONFIG)
    // ======================================================
    
    /** * Mo hinh quan ly du an ap dung.
     * Gia tri: SCRUM (Co Sprint, Backlog), KANBAN (Luong cong viec lien tuc), WATERFALL. 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "model", nullable = false)
    private ProjectModel model;

    /** * Cau hinh bo sung luu tru duoi dang JSON.
     * Chua cac thiet lap mac dinh nhu: Bo trang thai (Statuses), Loai Task (Task Types).
     */
    @Column(name = "configuration", columnDefinition = "JSON")
    private String configuration;

    // ======================================================
    // 4. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    /** Thoi diem loai du an nay duoc dinh nghia tren he thong. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public ProjectType() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong minh bach.
     */
    public ProjectType(Integer id, String typeName, String typeCode, String description, 
                       ProjectModel model, String configuration, LocalDateTime createdAt) {
        this.id = id;
        this.typeName = typeName;
        this.typeCode = typeCode;
        this.description = description;
        this.model = model;
        this.configuration = configuration;
        this.createdAt = createdAt;
    }
}