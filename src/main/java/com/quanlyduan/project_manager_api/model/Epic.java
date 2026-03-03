package com.quanlyduan.project_manager_api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// JPA & Hibernate
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Project Enums
import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;

/**
 * Entity đại diện cho một Epic.
 * Epic là một công việc lớn, thường bao gồm nhiều Tasks/User Stories.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "epics") // Đặt tên bảng là epics
public class Epic {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    // ==========================================
    // RELATIONSHIPS (Quan hệ Entity - Owning Side)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // Dự án chứa Epic này

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy; // Người tạo Epic

    // ==========================================
    // BASIC INFORMATION (Thông tin cơ bản)
    // ==========================================
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

    // ==========================================
    // TIMELINE (Dòng thời gian dự kiến)
    // ==========================================
    @Column(name = "start_date")
    private LocalDate startDate; // Ngày bắt đầu dự kiến

    @Column(name = "due_date")
    private LocalDate dueDate; // Ngày kết thúc/Hạn chót dự kiến

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
    // MappedBy trỏ đến tên thuộc tính "epic" trong Entity Task
    @OneToMany(mappedBy = "epic")
    private List<Task> tasks; // Quan hệ 1-N: Một Epic có nhiều Task

}