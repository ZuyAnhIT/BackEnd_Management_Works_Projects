package com.quanlyduan.project_manager_api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // (Sau này chúng ta sẽ inject JwtAuthenticationFilter và UserDetailsService ở đây)

    // Danh sách các URL không yêu cầu xác thực
    private static final String[] PUBLIC_URLS = {
            // -- API Auth --
            "/api/auth/**", // Cho phép tất cả API trong AuthController

            // -- SWAGGER UI --
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF vì chúng ta dùng API/JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PUBLIC_URLS).permitAll() // Cho phép các URL trong danh sách PUBLIC_URLS
                .anyRequest().authenticated() // Tất cả các request khác đều cần xác thực
            );
        
        // (Sau này, chúng ta sẽ thêm .authenticationProvider() và .addFilterBefore() cho JWT tại đây)

        return http.build();
    }
}