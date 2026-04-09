package com.quanlyduan.project_manager_api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.Gender;
import com.quanlyduan.project_manager_api.model.common.enums.UserStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity dai dien cho Nguoi dung (User) - Chu the chinh cua he thong.
 * Quan ly thong tin dang nhap, ho so ca nhan va trang thai hoat dong trong Worknet.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
public class User {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. XAC THUC & THONG TIN COT LOI (AUTH & CORE INFO)
    // ======================================================
    
    /** Email dung lam username duy nhat de dang nhap. */
    @Column(nullable = false, unique = true)
    private String email;

    /** Mat khau da duoc ma hoa (BCrypt/Argon2). */
    @Column(name = "password", nullable = false)
    private String password;

    /** Ho va ten day du cua nguoi dung. */
    @Column(name = "full_name", nullable = false)
    private String fullName;

    // ======================================================
    // 3. HO SO CA NHAN (PROFILE INFORMATION)
    // ======================================================
    
    /** Duong dan anh dai dien tren Storage Cloud. */
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** Gioi tinh nguoi dung (MALE, FEMALE, OTHER). */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    // ======================================================
    // 4. TRANG THAI TAI KHOAN (ACCOUNT STATUS)
    // ======================================================
    
    /** * Trang thai van hanh cua tai khoan.
     * Gia tri: ACTIVE (Hoat dong), LOCKED (Bi khoa), DELETED (Da xoa mem). 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    /** Xac dinh nguoi dung da kich hoat tai khoan qua Email chua. */
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified;

    // ======================================================
    // 5. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    /** Ghi nhan lan cuoi cung nguoi dung truy cap he thong. */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // 6. QUAN HE PHU (INVERSE RELATIONSHIPS)
    // ======================================================
    
    /** Danh sach cac Token xac thuc (Refresh Token, OTP). */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthToken> tokens;

    /** TAP HOP CAC VAI TRO CUA NGUOI DUNG */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public User() {
    }

    public User(Integer id, String email, String password, String fullName, 
                String avatarUrl, String phoneNumber, LocalDate dateOfBirth, 
                Gender gender, UserStatus status, Boolean isEmailVerified, 
                LocalDateTime lastLoginAt, LocalDateTime createdAt, 
                LocalDateTime updatedAt, List<AuthToken> tokens, Set<Role> roles) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.status = status;
        this.isEmailVerified = isEmailVerified;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tokens = tokens;
        this.roles = roles != null ? roles : new HashSet<>(); // Fallback an toan
    }
}