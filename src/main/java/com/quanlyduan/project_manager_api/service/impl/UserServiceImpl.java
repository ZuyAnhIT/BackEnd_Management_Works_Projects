// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/UserServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;
import com.quanlyduan.project_manager_api.dto.response.CompanyMembershipDTO;
import com.quanlyduan.project_manager_api.dto.response.UserProfileResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMembershipDTO;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.User; // Đã dịch
import com.quanlyduan.project_manager_api.model.UserRole; // Đã dịch
import com.quanlyduan.project_manager_api.model.CompanyMember; // Đã dịch
import com.quanlyduan.project_manager_api.model.WorkspaceMember; // Đã dịch
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository; // Đã dịch
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository; // Đã dịch
import com.quanlyduan.project_manager_api.repository.UserRepository; // Đã dịch
import com.quanlyduan.project_manager_api.repository.UserRoleRepository; // Đã dịch
import com.quanlyduan.project_manager_api.service.UserService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository; // Đã dịch
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository; // Đã dịch
    private final CompanyMemberRepository companyMemberRepository; // Đã dịch
    private final WorkspaceMemberRepository workspaceMemberRepository; // Đã dịch

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserRoleRepository userRoleRepository, CompanyMemberRepository companyMemberRepository, WorkspaceMemberRepository workspaceMemberRepository) { // Đã dịch
        this.userRepository = userRepository; // Đã dịch
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository = userRoleRepository; // Đã dịch
        this.companyMemberRepository = companyMemberRepository; // Đã dịch
        this.workspaceMemberRepository = workspaceMemberRepository; // Đã dịch
    }
    // (Sau này sẽ inject TokenRepository để hủy Refresh Token)

    // LOGIC THAY DOI MAT KHAU
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 1. Lấy thông tin người dùng đang đăng nhập
        User currentUser = getCurrentAuthenticatedUser(); // Đã dịch

        // 2. Validate mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) { // Đã dịch
            throw new BadRequestException("Incorrect old password"); // Đã dịch
        }

        // 3. Validate mật khẩu mới
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) { // Đã dịch
            throw new BadRequestException("New password must be different from the old password"); // Đã dịch
        }

        // 4. Validate mật khẩu xác nhận
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("Password confirmation does not match"); // Đã dịch
        }

        // 5. Hash và cập nhật mật khẩu mới
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword())); // Đã dịch

        // 6. Lưu vào CSDL
        userRepository.save(currentUser); // Đã dịch

        // 7. (Nâng cao) Thu hồi tất cả Refresh Token
        // Đây là bước quan trọng để bảo mật. Khi đổi mật khẩu,
        // tất cả các phiên đăng nhập ở thiết bị khác sẽ bị buộc đăng xuất.
        // tokenRepository.revokeAllUserRefreshTokens(currentUser.getId()); // Đã dịch
        // (Chúng ta sẽ implement chi tiết hàm revokeAll... này sau)
    }

    // --- Private Helper Method ---

    // LOGIC LAY THONG TIN DAY DU
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        // 1. Lấy người dùng (từ token)
        User currentUser = getCurrentAuthenticatedUser(); // Đã dịch

        // 2. Lấy vai trò cấp Hệ thống
        List<String> systemRoles = userRoleRepository.findByUser_Id(currentUser.getId()) // Đã dịch
                .stream()
                .map(userRole -> userRole.getRole().getRoleCode()) // Đã dịch
                .collect(Collectors.toList());

        // 3. Lấy vai trò cấp Công ty
        List<CompanyMembershipDTO> companyRoles = companyMemberRepository.findByUser_Id(currentUser.getId()) // Đã dịch
                .stream()
                .map(cm -> new CompanyMembershipDTO( // Đã dịch
                        cm.getCompany().getId(), // Đã dịch
                        cm.getCompany().getName(), // Đã dịch
                        cm.getRole().getRoleCode() // Đã dịch
                ))
                .collect(Collectors.toList());

        // 4. Lấy vai trò cấp Không gian
        List<WorkspaceMembershipDTO> workspaceRoles = workspaceMemberRepository.findByUser_Id(currentUser.getId()) // Đã dịch
                .stream()
                .map(wm -> new WorkspaceMembershipDTO( // Đã dịch
                        wm.getWorkspace().getId(), // Đã dịch
                        wm.getWorkspace().getName(), // Đã dịch
                        wm.getWorkspace().getCompany().getId(), // Đã dịch
                        wm.getRole().getRoleCode() // Đã dịch
                ))
                .collect(Collectors.toList());

        // 5. Xây dựng và trả về DTO
        return UserProfileResponse.builder()
                .id(currentUser.getId()) // Đã dịch
                .fullName(currentUser.getFullName()) // Đã dịch
                .email(currentUser.getEmail())
                .avatarUrl(currentUser.getAvatarUrl()) // Đã dịch
                .systemRoles(systemRoles)
                .companyMemberships(companyRoles)
                .workspaceMemberships(workspaceRoles)
                .build();
    }

    // LOGIC LAY NGUOI DUNG HIEN TAI
    private User getCurrentAuthenticatedUser() { // Đã dịch
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadRequestException("Authenticated user information not found."); // Đã dịch
        }
        
        String email = authentication.getName();
        return userRepository.findByEmail(email) // Đã dịch
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email)); // Đã dịch
    }
}