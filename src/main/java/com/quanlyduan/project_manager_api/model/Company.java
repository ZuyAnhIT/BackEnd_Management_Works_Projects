// File: src/main/java/com/quanlyduan/project_manager_api/model/Company.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
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
// Đặt tên bảng là companies
@Table(name = "companies")
/**
 * Entity đại diện cho một Công ty trong hệ thống.
 */
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh Công ty

    @Column(nullable = false)
    private String name; // Tên công ty

    @Column(name = "company_code", unique = true)
    private String companyCode; // Mã code công ty (có thể tự động tạo)

    @Column(name = "description")
    private String description; // Mô tả công ty

    @Column(name = "logo_url")
    private String logoUrl; // URL logo công ty

    @Column(name = "address")
    private String address; // Địa chỉ công ty

    @Column(name = "phone_number")
    private String phoneNumber; // Số điện thoại công ty

    @Column(name = "email")
    private String email; // Email công ty

    @Column(name = "website")
    private String website; // Website công ty

    @Column(name = "created_by_id", nullable = false)
    private Integer createdById; // ID của người dùng đã tạo công ty này (Audit field)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CompanyStatus status = CompanyStatus.ACTIVE; // Trạng thái công ty (ACTIVE, SUSPENDED, DELETED)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng
}