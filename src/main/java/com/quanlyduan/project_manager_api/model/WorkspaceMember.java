// File: src/main/java/com/quanlyduan/project_manager_api/model/WorkspaceMember.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workspace_members", uniqueConstraints = { // Đã dịch
    @UniqueConstraint(columnNames = {"workspace_id", "user_id"}) // Đã dịch
})
public class WorkspaceMember { // Đã dịch

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false) // Đã dịch
    private Workspace workspace; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Đã dịch
    private User user; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false) // Đã dịch
    private Role role; // Sử dụng Role (capDo = WORKSPACE)

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