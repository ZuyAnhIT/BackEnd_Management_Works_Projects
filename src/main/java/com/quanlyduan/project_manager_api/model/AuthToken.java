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
@Table(name = "auth_tokens") // Đã dịch
public class AuthToken { // Đã dịch

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Đã dịch
    private User user; // Đã dịch

    @Column(nullable = false, unique = true)
    private String token; // Sẽ chứa OTP cho việc xác thực email

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false) // Đã dịch
    private TokenType tokenType; // Đã dịch

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) // Đã dịch
    private TokenStatus status = TokenStatus.ACTIVE; // Đã dịch

    @Column(name = "expires_at", nullable = false) // Đã dịch
    private LocalDateTime expiresAt; // Đã dịch

    @Column(name = "ip_address") // Đã dịch
    private String ipAddress; // Đã dịch

    @Column(name = "user_agent") // Đã dịch
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false) // Đã dịch
    private LocalDateTime createdAt; // Đã dịch

    @Column(name = "last_used_at") // Đã dịch
    private LocalDateTime lastUsedAt; // Đã dịch
}