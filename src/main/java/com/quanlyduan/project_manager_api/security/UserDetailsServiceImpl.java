package com.quanlyduan.project_manager_api.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.model.User; 
import com.quanlyduan.project_manager_api.repository.UserRepository; 

/**
 * Service trien khai UserDetailsService cua Spring Security.
 * Chịu trách nhiệm truy vấn thông tin người dùng từ cơ sở dữ liệu để phục vụ quá trình xác thực.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final String ERR_USER_NOT_FOUND = "User not found with email: %s";

    private final UserRepository userRepository; 

    public UserDetailsServiceImpl(UserRepository userRepository) { 
        this.userRepository = userRepository; 
    }

    /**
     * Tai thong tin chi tiet nguoi dung dua tren Email (Username).
     * Phuong thuc nay duoc Spring Security tu dong goi trong qua trinh xac thuc JWT.
     * * @param email Email dang nhap cua nguoi dung.
     * @return Doi tuong UserPrincipal chua thong tin xac thuc va quyen han.
     * @throws UsernameNotFoundException neu khong tim thay Email trong he thong.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // 1. Tim kiem nguoi dung trong Database
        User user = userRepository.findByEmail(email) 
            .orElseThrow(() -> new UsernameNotFoundException(String.format(ERR_USER_NOT_FOUND, email)));

        // 2. Chuyen doi Entity User sang UserPrincipal (trien khai UserDetails)
        return UserPrincipal.create(user);
    }
}