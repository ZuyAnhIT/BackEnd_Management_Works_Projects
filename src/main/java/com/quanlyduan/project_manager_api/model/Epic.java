// File: src/main/java/com/quanlyduan/project_manager_api/model/Epic.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
// Đặt tên bảng là epics
@Table(name = "epics")
/**
 * Entity đại diện cho một Epic.
 * Epic là một công việc lớn, thường bao gồm nhiều Tasks/User Stories.
 */
public class Epic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // Dự án chứa Epic này

    @Column(name = "name", nullable = false)
    private String name; // Tên Epic

    @Column(name = "epic_code", length = 50)
    private String epicCode; // Mã code Epic (ví dụ: PROJ-E-1)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Mô tả Epic

    @Column(name = "color", length = 7)
    private String color; // Mã màu hex cho Epic (ví dụ: #FF0000)

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private EpicStatus status = EpicStatus.OPEN; // Trạng thái của Epic (Mặc định là OPEN)

    @Column(name = "start_date")
    private LocalDate startDate; // Ngày bắt đầu dự kiến

    @Column(name = "due_date")
    private LocalDate dueDate; // Ngày kết thúc/Hạn chót dự kiến

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy; // Người tạo Epic

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

    // Quan hệ nghịch đảo: Một Epic có nhiều Task
    // mappedBy trỏ đến tên thuộc tính "epic" trong Entity Task
    @OneToMany(mappedBy = "epic")
    private List<Task> tasks;
}