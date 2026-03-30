package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.TokenStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;

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
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity luu tru va quan ly vong doi cua cac loai Token xac thuc.
 * Bao gom: Refresh Token, Reset Password Token, Email OTP.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "auth_tokens")
public class AuthToken {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Nguoi dung so huu Token nay. Su dung LAZY fetch de toi uu hieu suat. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ======================================================
    // 3. THONG TIN COT LOI (CORE TOKEN DATA)
    // ======================================================
    
    /** Gia tri chuoi cua Token (da ma hoa hoac OTP thuan). */
    @Column(nullable = false, unique = true)
    private String token; 

    /** Loai Token (vi du: ACCESS, REFRESH, RESET_PASSWORD, OTP). */
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType; 

    /** * Trang thai hien tai cua Token.
     * Gia tri: ACTIVE (Con han), REVOKED (Bi thu hoi), EXPIRED (Het han). 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TokenStatus status; 

    // ======================================================
    // 4. QUAN LY THOI GIAN (TIMELINE)
    // ======================================================
    
    /** Thoi diem chinh xac Token se bi vo hieu hoa. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; 

    /** Thoi diem khoi tao Token (tu dong sinh bơi Hibernate). */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt; 

    /** Lan cuoi cung Token nay duoc su dung de xac thuc. */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt; 

    // ======================================================
    // 5. THONG TIN TRUY VET (TRACKING INFO)
    // ======================================================
    
    /** Dia chi IP cua thiet bi yeu cau khoi tao Token. */
    @Column(name = "ip_address")
    private String ipAddress; 

    /** Thong tin trinh duyet/thiet bi su dung (User Agent). */
    @Column(name = "user_agent")
    private String userAgent; 

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public AuthToken() {
    }

    public AuthToken(Integer id, User user, String token, TokenType tokenType, 
                     TokenStatus status, LocalDateTime expiresAt, LocalDateTime createdAt, 
                     LocalDateTime lastUsedAt, String ipAddress, String userAgent) {
        this.id = id;
        this.user = user;
        this.token = token;
        this.tokenType = tokenType;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}