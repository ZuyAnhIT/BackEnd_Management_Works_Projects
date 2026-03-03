package com.quanlyduan.project_manager_api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// JPA & Hibernate
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Project Enums
import com.quanlyduan.project_manager_api.model.common.enums.Gender;
import com.quanlyduan.project_manager_api.model.common.enums.UserStatus;

/**
 * Entity đại diện cho người dùng (User) trong hệ thống.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") // Đặt tên bảng là users
public class User {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    // ==========================================
    // AUTHENTICATION & CORE INFO (Thông tin cốt lõi & Đăng nhập)
    // ==========================================
    @Column(nullable = false, unique = true)
    private String email; // Email (dùng làm username, phải là duy nhất)

    @Column(name = "password", nullable = false)
    private String password; // Mật khẩu đã hash

    @Column(name = "full_name", nullable = false)
    private String fullName; // Họ và tên đầy đủ

    // ==========================================
    // PROFILE INFORMATION (Thông tin cá nhân)
    // ==========================================
    @Column(name = "avatar_url")
    private String avatarUrl; // URL ảnh đại diện

    @Column(name = "phone_number")
    private String phoneNumber; // Số điện thoại

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth; // Ngày sinh

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender; // Giới tính

    // ==========================================
    // ACCOUNT STATUS & FLAGS (Trạng thái tài khoản)
    // ==========================================
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE; // Trạng thái tài khoản (ACTIVE, LOCKED, DELETED)

    @Builder.Default
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false; // Cờ xác định email đã được xác minh chưa

    // ==========================================
    // TIMESTAMPS & AUDIT (Thời gian hệ thống)
    // ==========================================
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt; // Thời điểm đăng nhập gần nhất

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo tài khoản

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

    // ==========================================
    // INVERSE RELATIONSHIPS (Quan hệ nghịch đảo)
    // ==========================================
    // Quan hệ nghịch đảo: Một User có nhiều AuthToken (Refresh Token, OTP,...)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthToken> tokens;

}