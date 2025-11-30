// File: src/main/java/com/quanlyduan/project_manager_api/model/Tag.java
package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter // 1. Thay @Data bằng @Getter và @Setter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
// Đặt tên bảng là tags
@Table(name = "tags", uniqueConstraints = {
    // Đảm bảo tên Tag là duy nhất trong phạm vi một dự án
    @UniqueConstraint(columnNames = {"name", "project_id"})
})
// 2. QUAN TRỌNG: Chỉ tính hashCode và equals dựa trên các trường được đánh dấu @Include
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * Entity đại diện cho một Tag (Nhãn) dùng để phân loại Task trong Dự án.
 */
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // 3. Chỉ dùng ID để so sánh và tính hash
    private Integer id; // ID định danh

    @Column(nullable = false, length = 100)
    private String name; // Tên Tag

    @Column(length = 7)
    private String color; // Mã màu hex của Tag

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả Tag

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude // 4. Ngắt vòng lặp khi in log (Project -> Tag -> Project)
    private Project project; // Dự án chứa Tag này

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @ToString.Exclude
    private User createdBy; // Người tạo Tag

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

    // Quan hệ Many-to-Many nghịch đảo: Một Tag có thể được gán cho nhiều Task
    // mappedBy trỏ đến tên thuộc tính "tags" trong Entity Task
    @ManyToMany(mappedBy = "tags")
    @ToString.Exclude // 5. Ngắt vòng lặp log (Task -> Tag -> Task)
    @Builder.Default // Giúp builder không set null cho Set
    private Set<Task> tasks = new HashSet<>();
}