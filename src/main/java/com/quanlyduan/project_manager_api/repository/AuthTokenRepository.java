// File: src/main/java/com/quanlyduan/project_manager_api/repository/AuthTokenRepository.java
package com.quanlyduan.project_manager_api.repository;


import com.quanlyduan.project_manager_api.model.AuthToken; // Đã dịch
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Integer> { // Đã dịch
    
    Optional<AuthToken> findByTokenAndTokenType(String token, TokenType tokenType); // Đã dịch

    /**
     * Thu hồi (vô hiệu hóa) tất cả REFRESH token của một người dùng.
     * Dùng khi đổi mật khẩu hoặc reset mật khẩu để bảo mật.
     */
    @Modifying
    @Query("UPDATE AuthToken t SET t.status = 'REVOKED' " +
           "WHERE t.user.id = :userId " +
           "AND t.tokenType = 'REFRESH' " +
           "AND t.status = 'ACTIVE'")
    void revokeAllUserRefreshTokens(Integer userId);
}