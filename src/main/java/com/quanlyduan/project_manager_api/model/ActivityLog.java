package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

// JPA & Hibernate
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ==========================================
    // ACTOR INFORMATION (Người thực hiện)
    // ==========================================
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "action", nullable = false)
    private String action; // CREATE, UPDATE, DELETE, COMMENT, LOGIN

    // ==========================================
    // TARGET ENTITY INFORMATION (Đối tượng bị tác động)
    // ==========================================
    @Column(name = "entity_type")
    private String entityType; // PROJECT, TASK, SUBTASK

    @Column(name = "entity_id")
    private Integer entityId;
    
    @Column(name = "entity_name")
    private String entityName; // Lưu tên: "Fix Bug Login", "TechVision"

    @Column(name = "entity_code")
    private String entityCode; // Lưu mã: "ECOM-12", "CPW-11"

    // ==========================================
    // HIERARCHY CONTEXT (Phân cấp dữ liệu 4 cấp độ)
    // ==========================================
    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "workspace_id")
    private Integer workspaceId;

    @Column(name = "project_id")
    private Integer projectId;

    // ==========================================
    // CHANGE TRACKING DETAILS (Chi tiết thay đổi)
    // ==========================================
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // Chứa nội dung mô tả chi tiết (ví dụ: "changed status from To Do to Done")

    // ==========================================
    // SYSTEM & AUDIT INFORMATION (Thông tin hệ thống)
    // ==========================================
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;
    
    // Thêm @Builder.Default và khởi tạo giá trị mặc định luôn
    @Builder.Default
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now(); 
    
    // Hoặc nếu bạn muốn đồng nhất với created_at:
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}