// File: src/main/java/com/quanlyduan/project_manager_api/config/SecurityConfig.java
package com.quanlyduan.project_manager_api.config;

import com.quanlyduan.project_manager_api.security.UserDetailsServiceImpl;
import com.quanlyduan.project_manager_api.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Cho phép sử dụng @PreAuthorize trong Controller
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PasswordEncoder passwordEncoder;

    /**
     * Danh sách các đường dẫn API được phép truy cập công khai (không cần Token).
     * - /api/auth/**: Đăng nhập, Đăng ký, Quên mật khẩu...
     * - /v3/api-docs/**, /swagger-ui/**: Tài liệu API (Swagger).
     * - /uploads/**: Tài nguyên tĩnh (Hình ảnh đại diện, file đính kèm...).
     */
    private static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/invitations/**",
            "/api/files/**",
            "/uploads/**",
            "/api/plans",
            "/api/plans/**"
    };

    /**
     * Constructor thủ công để Inject các dependencies cần thiết.
     */
    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cấu hình Chuỗi bộ lọc bảo mật (Security Filter Chain).
     * Đây là trái tim của Spring Security.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Cấu hình CORS (Cross-Origin Resource Sharing)
            // Sử dụng bean corsConfigurationSource được định nghĩa bên dưới
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 2. Tắt CSRF (Cross-Site Request Forgery)
            // Vì chúng ta sử dụng JWT (Stateless) nên không cần bảo vệ CSRF như Session
            .csrf(AbstractHttpConfigurer::disable)

            // 3. Phân quyền truy cập URL
            .authorizeHttpRequests(auth -> auth
                // Các URL trong PUBLIC_URLS được phép truy cập tự do
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers("/api/payments/webhook").permitAll()
                // Tất cả các request còn lại BẮT BUỘC phải có Token xác thực
                .anyRequest().authenticated()
                
            )

            // 4. Quản lý Session
            // Thiết lập chế độ STATELESS: Server không lưu trạng thái đăng nhập (Session)
            // Mỗi request phải gửi kèm Token
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 5. Cấu hình Authentication Provider
            .authenticationProvider(authenticationProvider())

            // 6. Thêm Filter xác thực JWT
            // Filter này sẽ chạy TRƯỚC UsernamePasswordAuthenticationFilter
            // để kiểm tra Token trong Header trước khi xử lý đăng nhập truyền thống
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean cung cấp cơ chế xác thực.
     * Sử dụng DaoAuthenticationProvider để xác thực qua Database.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Cung cấp Service để tìm user trong DB
        authProvider.setUserDetailsService(userDetailsService);
        // Cung cấp Encoder để so sánh mật khẩu
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Bean quản lý xác thực chính của Spring Security.
     * Được sử dụng trong AuthController để thực hiện login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cấu hình CORS chi tiết.
     * Cho phép Frontend (http://localhost:3000) gọi API của Backend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Cho phép domain Frontend
        config.setAllowedOrigins(List.of("http://localhost:3000")); 
        
        // Cho phép các phương thức HTTP
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Cho phép tất cả các Header (bao gồm Authorization, Content-Type...)
        config.setAllowedHeaders(List.of("*"));
        
        // Cho phép gửi Credentials (Cookies, Auth Headers)
        config.setAllowCredentials(true);

        // Áp dụng cấu hình này cho toàn bộ các đường dẫn API (/**)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}