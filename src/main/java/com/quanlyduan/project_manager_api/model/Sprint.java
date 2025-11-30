// File: src/main/java/com/quanlyduan/project_manager_api/model/Sprint.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sprints")
/**
 * Entity đại diện cho một Sprint trong quy trình Scrum/Agile.
 * Sprint là một khoảng thời gian cố định để hoàn thành một lượng công việc đã chọn.
 */
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // Dự án chứa Sprint này

    @Column(name = "name", nullable = false)
    private String name; // Tên Sprint (Ví dụ: Sprint 1, Q3-2025)

    @Column(name = "sprint_code", length = 50)
    private String sprintCode; // Mã code Sprint (Ví dụ: PROJ-S1)

    @Column(name = "goal", columnDefinition = "TEXT")
    private String goal; // Mục tiêu của Sprint này

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SprintStatus status; // Trạng thái Sprint (NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED)

    @Column(name = "start_date")
    private LocalDateTime startDate; // Ngày bắt đầu thực tế/dự kiến

    @Column(name = "end_date")
    private LocalDateTime endDate; // Ngày kết thúc thực tế/dự kiến

    @Column(name = "duration_days")
    private Integer durationDays; // Độ dài Sprint theo ngày (Ví dụ: 14 ngày)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy; // Người tạo Sprint

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

    // Quan hệ nghịch đảo: Một Sprint có nhiều Task
    // mappedBy trỏ đến tên thuộc tính "sprint" trong Entity Task
    @OneToMany(mappedBy = "sprint")
    private Set<Task> tasks;
}