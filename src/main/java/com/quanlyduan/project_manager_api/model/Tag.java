package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter // 1. Thay @Data bằng @Getter và @Setter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "project_id"})
})
// 2. QUAN TRỌNG: Chỉ tính hashCode dựa trên các trường được đánh dấu @Include
@EqualsAndHashCode(onlyExplicitlyIncluded = true) 
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // 3. Chỉ dùng ID để so sánh và tính hash
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 7)
    private String color; 

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude // 4. Ngắt vòng lặp khi in log
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @ToString.Exclude
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Quan hệ Many-to-Many
    @ManyToMany(mappedBy = "tags")
    @ToString.Exclude // 5. Ngắt vòng lặp log
    // @EqualsAndHashCode tự động bỏ qua trường này vì ta đã dùng onlyExplicitlyIncluded ở class
    @Builder.Default // Giúp builder không set null cho Set
    private Set<Task> tasks = new HashSet<>();
}