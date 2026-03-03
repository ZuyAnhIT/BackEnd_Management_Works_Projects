package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

// JPA & Hibernate
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Project Enums
import com.quanlyduan.project_manager_api.model.common.enums.TokenStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;

/**
 * Entity lưu trữ các loại token xác thực: Refresh Token, Reset Password Token, Email OTP.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auth_tokens") // Đặt tên bảng là auth_tokens
public class AuthToken {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh của token

    // ==========================================
    // RELATIONSHIPS (Quan hệ Entity)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người dùng sở hữu token

    // ==========================================
    // TOKEN INFORMATION (Thông tin cốt lõi của Token)
    // ==========================================
    @Column(nullable = false, unique = true)
    private String token; // Giá trị chuỗi của token (hoặc OTP)

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType; // Loại token (ACCESS, REFRESH, RESET_PASSWORD,...)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TokenStatus status = TokenStatus.ACTIVE; // Trạng thái của token (ACTIVE, REVOKED, EXPIRED)

    // ==========================================
    // TIMESTAMPS (Thời gian)
    // ==========================================
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // Thời điểm token hết hạn

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo token

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt; // Thời điểm token được sử dụng lần cuối

    // ==========================================
    // TRACKING INFORMATION (Thông tin theo dõi)
    // ==========================================
    @Column(name = "ip_address")
    private String ipAddress; // Địa chỉ IP nơi token được tạo ra

    @Column(name = "user_agent")
    private String userAgent; // Thông tin trình duyệt/thiết bị

}