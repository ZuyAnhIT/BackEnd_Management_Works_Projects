// File: src/main/java/com/quanlyduan/project_manager_api/security/UserPrincipal.java
package com.quanlyduan.project_manager_api.security;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quanlyduan.project_manager_api.model.User; // Đã dịch
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections; // Sẽ cập nhật khi có Role

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Integer id;
    private final String fullName; // Đã dịch
    private final String email;
    private final Boolean isEmailVerified; // Đã dịch

    @JsonIgnore
    private final String password;

    // Sẽ cập nhật phần này khi có Role/Permission
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) { // Đã dịch
        // TODO: Sẽ cập nhật logic này để load Roles/Permissions từ DB
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

        return new UserPrincipal(
                user.getId(), // Đã dịch
                user.getFullName(), // Đã dịch
                user.getEmail(),
                user.getIsEmailVerified(), // Đã dịch
                user.getPassword(), // Đã dịch
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

    @Override
    public String getUsername() {
        return email; // Chúng ta dùng email làm username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Sau này có thể check user.getTrangThai() != UserStatus.TAM_KHOA
        return true; 
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Quan trọng: Chỉ cho phép đăng nhập nếu đã xác thực email
        return this.isEmailVerified; // Đã dịch
    }
}