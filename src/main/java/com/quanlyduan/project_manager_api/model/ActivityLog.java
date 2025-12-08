package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "activity_logs")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "action", nullable = false)
    private String action; // CREATE, UPDATE, DELETE, COMMENT, LOGIN

    @Column(name = "entity_type")
    private String entityType; // PROJECT, TASK, SUBTASK

    @Column(name = "entity_id")
    private Integer entityId;

    // --- Nâng cấp để lọc 4 cấp độ ---
    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "workspace_id")
    private Integer workspaceId;

    @Column(name = "project_id")
    private Integer projectId;
    // --------------------------------

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // Chứa nội dung mô tả chi tiết (ví dụ: "changed status from To Do to Done")

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}