// File: src/main/java/com/quanlyduan/project_manager_api/security/jwt/JwtTokenProvider.java
package com.quanlyduan.project_manager_api.security.jwt;

import com.quanlyduan.project_manager_api.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // Lấy chuỗi khóa bí mật (Base64 encoded) từ cấu hình
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Thời gian hết hạn của Access Token (tính bằng mili-giây)
    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    // Thời gian hết hạn của Refresh Token (tính bằng phút)
    @Value("${jwt.refresh-token-expiration-min}")
    private long refreshTokenExpirationMin;

    /**
     * Hàm helper: Giải mã chuỗi bí mật (jwtSecret) thành SecretKey.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ========================================================================
    // 1. LOGIC TẠO TOKEN
    // ========================================================================

    /**
     * Tạo Access Token.
     * Sử dụng Access Token Expiration (thường ngắn hạn).
     */
    public String generateAccessToken(Authentication authentication) {
        // Lấy User email (lưu trong subject) và thời gian hết hạn Access Token (ms)
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(userPrincipal.getUsername(), accessTokenExpirationMs);
    }

    /**
     * Tạo Refresh Token.
     * Sử dụng Refresh Token Expiration (thường dài hạn).
     */
    public String generateRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Chuyển đổi thời gian hết hạn từ phút sang mili-giây
        long expirationMs = refreshTokenExpirationMin * 60 * 1000;

        return generateToken(userPrincipal.getUsername(), expirationMs);
    }

    /**
     * Hàm tạo JWT Token chung.
     * @param subject Email người dùng (được lưu trong payload).
     * @param expirationMs Thời gian token hết hạn (mili-giây).
     */
    public String generateToken(String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(subject) // Đặt email người dùng làm Subject (User Identity)
                .issuedAt(now)    // Thời điểm tạo token
                .expiration(expiryDate) // Thời điểm token hết hạn
                .signWith(getSigningKey(), Jwts.SIG.HS256) // Ký token bằng thuật toán HS256 và Secret Key
                .compact(); // Nén token thành chuỗi cuối cùng
    }

    // ========================================================================
    // 2. LOGIC PHÂN TÍCH TOKEN
    // ========================================================================

    /**
     * Lấy email (Subject) từ JWT Token.
     * @param token Chuỗi JWT.
     * @return Email của người dùng.
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey()) // Xác thực chữ ký token
                .build()
                .parseSignedClaims(token)
                .getPayload(); // Lấy payload (body)

        return claims.getSubject(); // Subject chính là Email
    }

    /**
     * Xác thực JWT Token.
     * @param authToken Chuỗi JWT cần xác thực.
     * @return true nếu token hợp lệ và chưa hết hạn, ngược lại là false.
     */
    public boolean validateToken(String authToken) {
        try {
            // Nếu quá trình parse thành công, token hợp lệ (Signature khớp và chưa hết hạn)
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (Exception ex) {
            // Bắt lỗi và log lại (ví dụ: SignatureException, ExpiredJwtException, MalformedJwtException)
            logger.error("JWT validation failed: {}", ex.getMessage());
        }
        return false;
    }
}