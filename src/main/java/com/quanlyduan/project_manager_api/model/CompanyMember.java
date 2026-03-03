package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Project Enums
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

/**
 * Entity lưu trữ mối quan hệ thành viên giữa User và Company.
 * Đây là bảng liên kết (Join Table) mở rộng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "company_members", 
    uniqueConstraints = { 
        // Đảm bảo mỗi người dùng chỉ có một vai trò tại một công ty (Unique Company-User Pair)
        @UniqueConstraint(columnNames = {"company_id", "user_id"}) 
    }
)
public class CompanyMember { 

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh của mối quan hệ

    // ==========================================
    // RELATIONSHIPS (Quan hệ Entity)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // Công ty liên quan

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người dùng liên quan

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Vai trò của thành viên trong công ty (liên kết với bảng Role)

    // ==========================================
    // MEMBER DETAILS (Thông tin thành viên)
    // ==========================================
    @Column(name = "job_title")
    private String jobTitle; // Chức danh/Chức vụ

    @Column(name = "department")
    private String department; // Phòng ban

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE; // Trạng thái thành viên (ACTIVE, SUSPENDED, REMOVED)

    // ==========================================
    // TIMESTAMPS (Thời gian)
    // ==========================================
    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt; // Thời điểm tham gia/tạo bản ghi

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

}