// File: src/main/java/com.quanlyduan/project_manager_api/security/UserDetailsServiceImpl.java
package com.quanlyduan.project_manager_api.security;

import com.quanlyduan.project_manager_api.model.User; 
import com.quanlyduan.project_manager_api.repository.UserRepository; 
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service triển khai UserDetailsService của Spring Security.
 * Chịu trách nhiệm tải thông tin chi tiết người dùng (UserDetails) từ Database.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository; 

    // Constructor thủ công để inject UserRepository
    public UserDetailsServiceImpl(UserRepository userRepository) { 
        // Comment: Inject UserRepository
        this.userRepository = userRepository; 
    }

    /**
     * Tải thông tin người dùng dựa trên tên người dùng (ở đây là email).
     * Phương thức này được Spring Security gọi khi cần xác thực người dùng.
     * * @param email Email đăng nhập (được sử dụng làm Username)
     * @throws UsernameNotFoundException Nếu không tìm thấy người dùng trong DB
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // 1. Load người dùng từ DB bằng email
        User user = userRepository.findByEmail(email) 
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Convert Entity User sang đối tượng UserPrincipal (UserDetails) của hệ thống
        return UserPrincipal.create(user);
    }
}