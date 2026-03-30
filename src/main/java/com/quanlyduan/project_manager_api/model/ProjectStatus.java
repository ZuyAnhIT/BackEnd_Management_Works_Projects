package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity dai dien cho mot Trang thai Task (hoac mot cot tren bang Kanban/Scrum).
 * Cho phep tuy chinh quy trinh lam viec (Workflow) rieng biet cho tung Du an.
 */
@Getter
@Setter
@Builder
@Entity
@Table(
    name = "project_statuses", 
    uniqueConstraints = {
        /** Dam bao ten cot la duy nhat trong pham vi mot Du an de tranh nham lan Workflow. */
        @UniqueConstraint(columnNames = {"project_id", "name"})
    }
)
public class ProjectStatus {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Du an (Project) so huu trang thai nay. Su dung LAZY fetch de toi uu hieu suat. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ======================================================
    // 3. CHI TIET TRANG THAI (STATUS DETAILS)
    // ======================================================
    
    /** Ten hien thi cua cot (vi du: "To Do", "In Progress", "Testing", "Done"). */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Ma mau HEX dung de to mau cho tieu de cot hoac Badge (vi du: "#3498db"). */
    @Column(name = "color", length = 7)
    private String color;

    // ======================================================
    // 4. HIEN THI & HANH VI (DISPLAY & BEHAVIOR)
    // ======================================================
    
    /** * Thu tu sap xep cua cot tren bang Kanban (tu trai sang phai).
     * Thuong bat dau tu 0, 1, 2...
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /** * Co xac dinh day co phai la trang thai ket thuc (Done/Closed) hay khong.
     * Dung de he thong tu dong tinh toan % tien do hoan thanh cua Du an/Epic/Sprint.
     */
    @Column(name = "is_completed_status", nullable = false)
    private Boolean isCompletedStatus;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public ProjectStatus() {
    }

    public ProjectStatus(Integer id, Project project, String name, String color, 
                         Integer sortOrder, Boolean isCompletedStatus) {
        this.id = id;
        this.project = project;
        this.name = name;
        this.color = color;
        this.sortOrder = sortOrder;
        this.isCompletedStatus = isCompletedStatus;
    }
}