package com.quanlyduan.project_manager_api.security;

import com.quanlyduan.project_manager_api.model.NguoiDung;
import com.quanlyduan.project_manager_api.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepository;

    public UserDetailsServiceImpl(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Load người dùng từ DB bằng email
        NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        // Convert NguoiDung sang UserPrincipal
        return UserPrincipal.create(user);
    }
}