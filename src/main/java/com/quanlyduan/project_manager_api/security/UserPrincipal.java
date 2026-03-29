package com.quanlyduan.project_manager_api.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quanlyduan.project_manager_api.model.User;

import lombok.Getter;

/**
 * Lop dai dien cho danh tinh nguoi dung da duoc xac thuc (Security Principal).
 * Luu tru thong tin co ban va danh sach quyen han trong Spring Security Context.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Integer id;
    private final String fullName;
    private final String email;
    private final Boolean isEmailVerified;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    // Khoi tao thu cong thay vi dung @AllArgsConstructor
    public UserPrincipal(Integer id, String fullName, String email, Boolean isEmailVerified, 
                         String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.isEmailVerified = isEmailVerified;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Factory Method de tao doi tuong UserPrincipal tu thuc the User (Entity).
     * @param user Doi tuong User truy van tu co so du lieu.
     * @return UserPrincipal hop le cho Spring Security.
     */
    public static UserPrincipal create(User user) {
        // Logic load quyen han (Authorities) se duoc bo sung tai day khi he thong phan quyen hoan thien
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

    // ========================================================================
    // TRIEN KHAI CAC PHUONG THUC CUA USERDETAILS
    // ========================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Su dung Email lam dinh danh duy nhat (Username) cho qua trinh dang nhap.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Tai khoan chi duoc kich hoat neu Email da duoc xac thuc thanh cong.
     */
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isEmailVerified);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Mac dinh tai khoan khong bi khoa. 
     * Sau nay co the mo rong de kiem tra thuoc tinh 'status' cua User.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}