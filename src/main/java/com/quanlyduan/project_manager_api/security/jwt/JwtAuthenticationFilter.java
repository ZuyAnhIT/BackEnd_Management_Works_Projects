package com.quanlyduan.project_manager_api.security.jwt;

import java.io.IOException;

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

import com.quanlyduan.project_manager_api.security.UserDetailsServiceImpl;

/**
 * Filter xác thực JWT.
 * Lớp này chạy ở đầu chuỗi Filter của Spring Security để kiểm tra JWT Token
 * trong Header và thiết lập thông tin người dùng vào Security Context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Khai báo các hằng số cho Header và Token
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    // Khởi tạo thủ công để tiêm phụ thuộc (Dependency Injection)
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsServiceImpl userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Xử lý kiểm tra Token và thiết lập ngữ cảnh bảo mật cho mỗi Request.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. Trích xuất JWT từ Header
            String jwt = getJwtFromRequest(request);

            // 2. Xác thực tính hợp lệ của Token
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                
                // 3. Truy xuất thông tin định danh (Email) từ Token
                String email = tokenProvider.getEmailFromToken(jwt);

                // 4. Tải thông tin chi tiết người dùng từ cơ sở dữ liệu
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                // 5. Khởi tạo đối tượng xác thực (Authentication)
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                // 6. Gán chi tiết thông tin request vào đối tượng xác thực
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Thiết lập đối tượng xác thực vào Security Context của hệ thống
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Ghi nhật ký lỗi nếu quá trình xác thực thất bại (Token hết hạn, sai chữ ký, v.v.)
            logger.error("Could not set user authentication in security context.", ex);
        }

        // Tiếp tục chuyển quyền xử lý cho các Filter tiếp theo trong chuỗi
        filterChain.doFilter(request, response);
    }

    /**
     * Hàm hỗ trợ: Trích xuất chuỗi JWT nguyên bản từ Header "Authorization".
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX_LENGTH);
        }
        
        return null;
    }
}