// File: src/main/java/com/quanlyduan/project_manager_api/model/CompanyMember.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_members", uniqueConstraints = { // Đã dịch
    @UniqueConstraint(columnNames = {"company_id", "user_id"}) // Đã dịch
})
public class CompanyMember { // Đã dịch

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false) // Đã dịch
    private Company company; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Đã dịch
    private User user; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false) // Đã dịch
    private Role role; // Sử dụng quan hệ, không phải ENUM

    @Column(name = "job_title") // Đã dịch
    private String jobTitle; // Đã dịch

    @Column(name = "department") // Đã dịch
    private String department; // Đã dịch

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) // Đã dịch
    private MemberStatus status = MemberStatus.ACTIVE; // Đã dịch

    @CreationTimestamp
    @Column(name = "joined_at") // Đã dịch
    private LocalDateTime joinedAt; // Đã dịch

    @UpdateTimestamp
    @Column(name = "updated_at") // Đã dịch
    private LocalDateTime updatedAt; // Đã dịch
}