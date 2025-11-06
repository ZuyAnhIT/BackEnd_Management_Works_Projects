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
@Table(name = "companies") // Đã dịch
public class Company { // Đã dịch

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Đã dịch

    @Column(nullable = false)
    private String name; // Đã dịch

    @Column(name = "company_code", unique = true) // Đã dịch
    private String companyCode; // Đã dịch
    // Có thể tự động tạo từ tên

    @Column(name = "description")
    private String description; // Đã dịch

    @Column(name = "logo_url")
    private String logoUrl; // Đã dịch (logo -> logoUrl)

    @Column(name = "address")
    private String address; // Đã dịch

    @Column(name = "phone_number")
    private String phoneNumber; // Đã dịch

    @Column(name = "email")
    private String email;

    @Column(name = "website")
    private String website;

    @Column(name = "created_by_id", nullable = false) // Đã dịch
    private Integer createdById; // Đã dịch
    // Chỉ lưu ID người tạo

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) // Đã dịch
    private CompanyStatus status = CompanyStatus.ACTIVE; // Đã dịch

    @CreationTimestamp
    @Column(name = "created_at", updatable = false) // Đã dịch
    private LocalDateTime createdAt; // Đã dịch

    @UpdateTimestamp
    @Column(name = "updated_at") // Đã dịch
    private LocalDateTime updatedAt; // Đã dịch
}