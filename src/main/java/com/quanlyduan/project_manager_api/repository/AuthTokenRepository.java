package com.quanlyduan.project_manager_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.AuthToken;
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;

/**
 * Kho lưu trữ dữ liệu cho thực thể AuthToken.
 * Quản lý vòng đời của các loại mã xác thực như Refresh Token, OTP và Reset Token.
 */
@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Integer> {

    // Khai báo các câu truy vấn tĩnh để tránh hardcode trong phương thức
    String REVOKE_TOKENS_QUERY = "UPDATE AuthToken t SET t.status = 'REVOKED' " +
                                 "WHERE t.user.id = :userId " +
                                 "AND t.tokenType = 'REFRESH' " +
                                 "AND t.status = 'ACTIVE'";

    /**
     * Tìm kiếm mã xác thực dựa trên giá trị chuỗi và loại Token cụ thể.
     * @param token Chuỗi mã xác thực cần tìm.
     * @param tokenType Loại mã (ví dụ: REFRESH, OTP).
     * @return Kết quả tìm kiếm dưới dạng Optional.
     */
    Optional<AuthToken> findByTokenAndTokenType(String token, TokenType tokenType);

    /**
     * Vô hiệu hóa (thu hồi) toàn bộ Refresh Token đang hoạt động của một người dùng.
     * Thường được sử dụng khi người dùng đổi mật khẩu hoặc thực hiện Reset mật khẩu.
     * @param userId ID của người dùng cần thu hồi Token.
     */
    @Modifying
    @Query(REVOKE_TOKENS_QUERY)
    void revokeAllUserRefreshTokens(Integer userId);
}