package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Project Enums
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;

/**
 * Entity đại diện cho một Công ty (Tenant) trong hệ thống SaaS Worknet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "companies")
public class Company {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh Công ty

    // ==========================================
    // BASIC INFORMATION (Thông tin cơ bản)
    // ==========================================
    
    @Column(nullable = false)
    private String name; // Tên công ty

    @Column(name = "company_code", unique = true)
    private String companyCode; // Mã code công ty (có thể tự động tạo)

    @Column(name = "description")
    private String description; // Mô tả công ty

    @Column(name = "logo_url")
    private String logoUrl; // URL logo công ty

    // ==========================================
    // CONTACT INFORMATION (Thông tin liên hệ)
    // ==========================================
    
    @Column(name = "address")
    private String address; // Địa chỉ công ty

    @Column(name = "phone_number")
    private String phoneNumber; // Số điện thoại công ty

    @Column(name = "email")
    private String email; // Email công ty

    @Column(name = "website")
    private String website; // Website công ty

    // ==========================================
    // SAAS & RESOURCE TRACKING (Quản lý tài nguyên SaaS)
    // ==========================================
    
    /**
     * Tổng dung lượng lưu trữ hiện tại mà công ty đang sử dụng (tính bằng Bytes).
     * Phục vụ việc đối chiếu với giới hạn (max_storage_gb) của Gói cước (Subscription Plan).
     */
    @Builder.Default
    @Column(name = "current_storage_bytes")
    private Long currentStorageBytes = 0L;

    /**
     * Cờ đánh dấu công ty đã được xác thực danh tính (Tích xanh/Verified Tenant).
     */
    @Builder.Default
    @Column(name = "is_verified_tenant")
    private Boolean isVerifiedTenant = false;

    // ==========================================
    // SYSTEM & STATUS INFORMATION (Hệ thống & Trạng thái)
    // ==========================================
    
    @Column(name = "created_by_id", nullable = false)
    private Integer createdById; // ID của người dùng đã tạo công ty này (Audit field)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CompanyStatus status = CompanyStatus.ACTIVE; // Trạng thái công ty (ACTIVE, SUSPENDED, DELETED)

    // ==========================================
    // TIMESTAMPS (Thời gian)
    // ==========================================
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

    @OneToOne(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private CompanySubscription subscription;

}