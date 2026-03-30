package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity luu tru moi quan he thanh vien giua User va Project.
 * Day la bang lien ket (Join Table) mo rong, xac dinh vai tro va quyen han 
 * cua tung nguoi dung ben trong pham vi mot Du an cu the.
 */
@Getter
@Setter
@Builder
@Entity
@Table(
    name = "project_members", 
    uniqueConstraints = {
        /** Dam bao moi nguoi dung chi co duy nhat mot ban ghi thanh vien tai mot Du an. */
        @UniqueConstraint(columnNames = {"project_id", "user_id"})
    }
)
public class ProjectMember {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Du an (Project) ma nguoi dung dang tham gia. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Nguoi dung tro thanh thanh vien cua du an. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Vai tro (Role) duoc gan cho thanh vien ben trong Du an nay (vi du: Developer, QA, Lead). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // ======================================================
    // 3. TRANG THAI & HE THONG (STATUS & AUDIT)
    // ======================================================

    /** * Trang thai hoat dong cua thanh vien trong Du an.
     * Gia tri: ACTIVE (Dang tham gia), REMOVED (Da roi khoi), SUSPENDED. 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    /** Thoi diem nguoi dung gia nhap vao Du an (tu dong sinh boi Hibernate). */
    @CreationTimestamp
    @Column(name = "joined_at", updatable = false, nullable = false)
    private LocalDateTime joinedAt;

    /** Thoi diem cap nhat thong tin thanh vien lan cuoi cung. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public ProjectMember() {
    }

    public ProjectMember(Integer id, Project project, User user, Role role, 
                         MemberStatus status, LocalDateTime joinedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.project = project;
        this.user = user;
        this.role = role;
        this.status = status;
        this.joinedAt = joinedAt;
        this.updatedAt = updatedAt;
    }
}