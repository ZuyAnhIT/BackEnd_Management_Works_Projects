package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity dai dien cho mot Quyen han (Permission) - Don vi nho nhat trong he thong phan quyen RBAC.
 * Dung de kiem soat truy cap den cac API hoac tinh nang cu the dua tren ma code duy nhat.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "permissions")
public class Permission {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. CHI TIET QUYEN HAN (PERMISSION DETAILS)
    // ======================================================
    
    /** * Ma code duy nhat dung de kiem tra quyen trong code (vi du: "TASK_CREATE", "PROJECT_DELETE"). 
     * Day la truong quan trong nhat de logic phan quyen (Security Filter) hoat dong.
     */
    @Column(name = "permission_code", nullable = false, unique = true)
    private String permissionCode;

    /** Ten hien thi than thien voi nguoi dung (vi du: "Tao moi cong viec"). */
    @Column(name = "permission_name", nullable = false)
    private String permissionName;

    /** * Nhom chuc nang (vi du: "TASK_MGMT", "SYSTEM_CONFIG"). 
     * Ho tro Frontend gom nhom khi hien thi danh sach check-box phan quyen. 
     */
    @Column(name = "group_name")
    private String groupName;

    // ======================================================
    // 3. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    /** Thoi diem quyen han nay duoc khoi tao trong he thong. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public Permission() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong minh bach.
     */
    public Permission(Integer id, String permissionCode, String permissionName, 
                      String groupName, LocalDateTime createdAt) {
        this.id = id;
        this.permissionCode = permissionCode;
        this.permissionName = permissionName;
        this.groupName = groupName;
        this.createdAt = createdAt;
    }
}