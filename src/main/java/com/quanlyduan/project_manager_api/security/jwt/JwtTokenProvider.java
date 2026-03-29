package com.quanlyduan.project_manager_api.security.jwt;

import java.util.Date;
import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.quanlyduan.project_manager_api.security.UserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Thành phần chịu trách nhiệm tạo, phân tích và xác thực JSON Web Token (JWT).
 * Quản lý cả Access Token ngắn hạn và Refresh Token dài hạn.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final long MS_PER_MINUTE = 60_000L;

    private final String jwtSecret;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMin;

    /**
     * Khởi tạo thủ công và tiêm các giá trị cấu hình từ tệp ứng dụng.
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-min}") long refreshTokenExpirationMin) {
        this.jwtSecret = jwtSecret;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMin = refreshTokenExpirationMin;
    }

    // ========================================================================
    // TẠO TOKEN (TOKEN GENERATION)
    // ========================================================================

    /**
     * Tạo Access Token dựa trên thông tin xác thực của người dùng.
     */
    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(userPrincipal.getUsername(), accessTokenExpirationMs);
    }

    /**
     * Tạo Refresh Token với thời gian hết hạn dài hơn để duy trì phiên đăng nhập.
     */
    public String generateRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        long expirationMs = refreshTokenExpirationMin * MS_PER_MINUTE;
        return generateToken(userPrincipal.getUsername(), expirationMs);
    }

    /**
     * Phương thức tạo JWT chung cho cả Access và Refresh Token.
     * @param subject Danh tính người dùng (thường là Email).
     * @param expirationMs Thời gian sống của Token tính bằng mili-giây.
     */
    public String generateToken(String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // ========================================================================
    // PHÂN TÍCH & XÁC THỰC (PARSING & VALIDATION)
    // ========================================================================

    /**
     * Trích xuất Email (Subject) từ chuỗi Token đã ký.
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Kiểm tra tính hợp lệ về cấu trúc, chữ ký và thời hạn của Token.
     * @return true nếu Token hoàn toàn hợp lệ.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (Exception ex) {
            logger.error("JWT validation failed: {}", ex.getMessage());
        }
        return false;
    }

    // ========================================================================
    // TIỆN ÍCH NỘI BỘ (INTERNAL HELPERS)
    // ========================================================================

    /**
     * Giải mã chuỗi bí mật Base64 thành khóa ký bảo mật phù hợp với thuật toán HMAC.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}