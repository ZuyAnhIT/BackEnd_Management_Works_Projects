// File: src/main/java/com/quanlyduan/project_manager_api/model/Workspace.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
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
@Table(name = "workspaces") // Đã dịch
public class Workspace { // Đã dịch

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false) // Đã dịch
    private Company company; // Đã dịch

    @Column(name = "name", nullable = false) // Đã dịch
    private String name; // Đã dịch

    @Column(name = "workspace_code") // Đã dịch
    private String workspaceCode; // Đã dịch

    @Column(name = "description") // Đã dịch
    private String description; // Đã dịch

    @Column(name = "cover_image_url") // Đã dịch
    private String coverImageUrl; // Đã dịch

    @Builder.Default
    @Column(name = "color") // Đã dịch
    private String color = "#3498db"; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false) // Đã dịch
    private User createdBy; // Đã dịch

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) // Đã dịch
    private WorkspaceStatus status = WorkspaceStatus.ACTIVE; // Đã dịch

    @CreationTimestamp
    @Column(name = "created_at", updatable = false) // Đã dịch
    private LocalDateTime createdAt; // Đã dịch

    @UpdateTimestamp
    @Column(name = "updated_at") // Đã dịch
    private LocalDateTime updatedAt; // Đã dịch
}