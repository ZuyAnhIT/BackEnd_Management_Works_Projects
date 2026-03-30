package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity dai dien cho mot Tag (Nhan) dung de phan loai Task linh hoat.
 * Ho tro gan nhieu nhãn cho mot cong viec de de dang loc va tim kiem theo nghiep vu.
 */
@Getter 
@Setter
@Builder
@Entity
@Table(
    name = "tags", 
    uniqueConstraints = {
        /** Dam bao ten Tag la duy nhat trong pham vi mot Du an de tranh nham lan phan loai. */
        @UniqueConstraint(columnNames = {"name", "project_id"})
    }
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tag {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include 
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Du an (Project) so huu Tag nay. Su dung LAZY fetch de toi uu tai nguyen. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    private Project project;

    /** Nguoi dung thuc hien tao Tag moi. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    @ToString.Exclude
    private User createdBy;

    /** * Danh sach cac Tasks dang duoc gan Tag nay (Inverse side). 
     * Quan he Many-to-Many voi bang trung gian duoc dinh nghia ben phia Entity Task.
     */
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();

    // ======================================================
    // 3. THONG TIN CO BAN (BASIC INFO)
    // ======================================================
    
    /** Ten hien thi cua Tag (vi du: "Bug", "Enhancement", "Urgent"). */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Ma mau HEX dai dien cho Tag tren giao dien (vi du: "#e74c3c"). */
    @Column(name = "color", length = 7)
    private String color;

    /** Mo ta chi tiet ve muc dich su dung cua Tag. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ======================================================
    // 4. THONG TIN HE THONG (AUDIT INFO)
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

    public Tag() {
    }

    public Tag(Integer id, Project project, User createdBy, Set<Task> tasks, 
               String name, String color, String description, 
               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.project = project;
        this.createdBy = createdBy;
        this.tasks = tasks;
        this.name = name;
        this.color = color;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}