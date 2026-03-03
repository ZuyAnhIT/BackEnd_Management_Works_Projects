package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// JPA & Hibernate
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity đại diện cho một Tag (Nhãn) dùng để phân loại Task trong Dự án.
 */
// 1. Thay @Data bằng @Getter và @Setter để tránh lỗi vòng lặp
@Getter 
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "tags", 
    uniqueConstraints = {
        // Đảm bảo tên Tag là duy nhất trong phạm vi một dự án
        @UniqueConstraint(columnNames = {"name", "project_id"})
    }
)
// 2. QUAN TRỌNG: Chỉ tính hashCode và equals dựa trên các trường được đánh dấu @Include
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tag {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // 3. Chỉ dùng ID để so sánh và tính hash
    private Integer id; // ID định danh

    // ==========================================
    // BASIC INFORMATION (Thông tin cơ bản)
    // ==========================================
    @Column(nullable = false, length = 100)
    private String name; // Tên Tag

    @Column(length = 7)
    private String color; // Mã màu hex của Tag

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả Tag

    // ==========================================
    // RELATIONSHIPS (Quan hệ Entity - Owning Side)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude // 4. Ngắt vòng lặp khi in log (Project -> Tag -> Project)
    private Project project; // Dự án chứa Tag này

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @ToString.Exclude
    private User createdBy; // Người tạo Tag

    // ==========================================
    // TIMESTAMPS (Thời gian hệ thống)
    // ==========================================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

    // ==========================================
    // INVERSE RELATIONSHIPS (Quan hệ nghịch đảo)
    // ==========================================
    // Quan hệ Many-to-Many nghịch đảo: Một Tag có thể được gán cho nhiều Task
    // mappedBy trỏ đến tên thuộc tính "tags" trong Entity Task
    @ManyToMany(mappedBy = "tags")
    @ToString.Exclude // 5. Ngắt vòng lặp log (Task -> Tag -> Task)
    @Builder.Default // Giúp builder không set null cho Set
    private Set<Task> tasks = new HashSet<>();

}