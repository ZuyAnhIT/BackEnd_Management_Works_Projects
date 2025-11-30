// File: src/main/java/com.quanlyduan.project_manager_api/security/UserPrincipal.java
package com.quanlyduan.project_manager_api.security;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quanlyduan.project_manager_api.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Lớp chính chứa thông tin người dùng đã xác thực (Security Principal).
 * Dùng để lưu trữ thông tin cơ bản của User và quyền hạn trong Spring Security Context.
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    // ========================================================================
    // 1. THÔNG TIN CƠ BẢN & ĐỊNH DANH
    // ========================================================================

    private final Integer id;
    private final String fullName;
    private final String email;
    private final Boolean isEmailVerified; // Cờ xác thực email

    // ========================================================================
    // 2. THÔNG TIN BẢO MẬT & QUYỀN HẠN
    // ========================================================================

    @JsonIgnore // Không được serialize ra JSON (bảo mật)
    private final String password;

    // Danh sách quyền hạn (Roles/Permissions) của người dùng
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Phương thức Factory tĩnh: Tạo đối tượng UserPrincipal từ Entity User.
     * @param user Entity User từ Database.
     */
    public static UserPrincipal create(User user) {
        // [LƯU Ý]: Hiện tại, hàm này tạo danh sách quyền rỗng.
        // Logic sẽ được cập nhật sau để load Roles/Permissions từ DB.
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

        return new UserPrincipal(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getIsEmailVerified(),
                user.getPassword(),
                authorities
        );
    }
    
    // --- Implement các phương thức của UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Trả về định danh người dùng. Trong hệ thống này, chúng ta dùng email làm username.
     */
    @Override
    public String getUsername() {
        return email; 
    }

    /**
     * Cờ kiểm tra tài khoản có được phép sử dụng hay không.
     * QUAN TRỌNG: Chỉ cho phép đăng nhập thành công nếu email đã được xác thực.
     */
    @Override
    public boolean isEnabled() {
        return this.isEmailVerified; 
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true; // Mặc định là true (không hết hạn)
    }

    @Override
    public boolean isAccountNonLocked() {
        // Mặc định là true (không bị khóa). Sau này có thể kiểm tra UserStatus.
        return true; 
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Mặc định là true
    }
}