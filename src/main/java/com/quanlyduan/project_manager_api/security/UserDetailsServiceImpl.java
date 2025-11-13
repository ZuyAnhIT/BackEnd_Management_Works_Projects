// File: src/main/java/com/quanlyduan/project_manager_api/security/UserDetailsServiceImpl.java
package com.quanlyduan.project_manager_api.security;

import com.quanlyduan.project_manager_api.model.User; // Đã dịch
import com.quanlyduan.project_manager_api.repository.UserRepository; // Đã dịch
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository; // Đã dịch

    public UserDetailsServiceImpl(UserRepository userRepository) { // Đã dịch
        this.userRepository = userRepository; // Đã dịch
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Load người dùng từ DB bằng email
        User user = userRepository.findByEmail(email) // Đã dịch
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm được người dùng với email: " + email)); // Đã dịch

        // Convert NguoiDung sang UserPrincipal
        return UserPrincipal.create(user);
    }
}