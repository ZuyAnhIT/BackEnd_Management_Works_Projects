// File: src/main/java/com/quanlyduan/project_manager_api/model/AuthToken.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.TokenStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;
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
// Đặt tên bảng là auth_tokens
@Table(name = "auth_tokens")
/**
 * Entity lưu trữ các loại token xác thực: Refresh Token, Reset Password Token, Email OTP.
 */
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh của token

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người dùng sở hữu token

    @Column(nullable = false, unique = true)
    private String token; // Giá trị chuỗi của token (hoặc OTP)

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType; // Loại token (ACCESS, REFRESH, RESET_PASSWORD,...)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TokenStatus status = TokenStatus.ACTIVE; // Trạng thái của token (ACTIVE, REVOKED, EXPIRED)

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // Thời điểm token hết hạn

    @Column(name = "ip_address")
    private String ipAddress; // Địa chỉ IP nơi token được tạo ra

    @Column(name = "user_agent")
    private String userAgent; // Thông tin trình duyệt/thiết bị

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo token

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt; // Thời điểm token được sử dụng lần cuối
}