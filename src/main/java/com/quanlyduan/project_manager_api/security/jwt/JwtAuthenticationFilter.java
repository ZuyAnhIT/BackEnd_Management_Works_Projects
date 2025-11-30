// File: src/main/java/com/quanlyduan.project_manager_api/security/jwt/JwtAuthenticationFilter.java
package com.quanlyduan.project_manager_api.security.jwt;


import com.quanlyduan.project_manager_api.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter xác thực JWT.
 * Lớp này chạy ở đầu chuỗi Filter của Spring Security để kiểm tra JWT Token
 * trong Header và thiết lập thông tin người dùng vào Security Context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    // Constructor thủ công để inject dependencies
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsServiceImpl userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. Lấy chuỗi JWT (Access Token) từ Header
            String jwt = getJwtFromRequest(request);

            // 2. Kiểm tra tính hợp lệ và xác thực Token
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // 3. Lấy Email (Username) từ Token
                String email = tokenProvider.getEmailFromToken(jwt);

                // 4. Load chi tiết User (UserDetails) từ Database
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                // 5. Khởi tạo đối tượng xác thực (Authentication)
                // Password là null vì ta đã xác thực bằng Token (không cần password)
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                // 6. Gán thêm thông tin chi tiết của request (IP, Browser,...)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Thiết lập đối tượng xác thực vào Spring Security Context
                // Từ đây, các API @PreAuthorize có thể truy cập thông tin User
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Bắt lỗi khi không thể thiết lập xác thực (ví dụ: Token hết hạn, sai chữ ký)
            logger.error("Could not set user authentication in security context.", ex); // Đã dịch
        }

        // Chuyển quyền xử lý sang Filter tiếp theo trong chuỗi
        filterChain.doFilter(request, response);
    }

    /**
     * Hàm helper: Trích xuất chuỗi JWT (sau tiền tố "Bearer ") từ Header "Authorization".
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Kiểm tra xem Header có tồn tại và bắt đầu bằng "Bearer " không
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Bỏ qua 7 ký tự đầu ("Bearer ")
        }
        return null;
    }
}