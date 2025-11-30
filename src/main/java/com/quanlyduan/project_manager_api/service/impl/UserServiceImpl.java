// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/UserServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProfileRequest;
import com.quanlyduan.project_manager_api.dto.response.CompanyMembershipDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectMembershipDTO;
import com.quanlyduan.project_manager_api.dto.response.UserProfileResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMembershipDTO;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.AuthTokenRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.UserRoleRepository;
import com.quanlyduan.project_manager_api.service.FileStorageService;
import com.quanlyduan.project_manager_api.service.UserService;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AuthTokenRepository authTokenRepository;
    private final FileStorageService fileStorageService;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public UserServiceImpl(UserRepository userRepository,PasswordEncoder passwordEncoder, UserRoleRepository userRoleRepository, CompanyMemberRepository companyMemberRepository, WorkspaceMemberRepository workspaceMemberRepository, ProjectMemberRepository projectMemberRepository, AuthTokenRepository authTokenRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository = userRoleRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.authTokenRepository = authTokenRepository;
        this.fileStorageService = fileStorageService;

    }

    // ======================================================
    // 1. THAY ĐỔI MẬT KHẨU (CHANGE PASSWORD)
    // ======================================================
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 1. Lấy thông tin người dùng đang đăng nhập
        User currentUser = getCurrentAuthenticatedUser();

        // 2. Validate mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Incorrect old password.");
        }

        // 3. Validate mật khẩu mới (phải khác mật khẩu cũ)
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("New password must be different from the old password.");
        }

        // 4. Validate mật khẩu xác nhận (confirm password)
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Password confirmation does not match.");
        }

        // 5. Hash và cập nhật mật khẩu mới
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 6. Lưu vào CSDL
        userRepository.save(currentUser);

        // 7. Thu hồi tất cả Refresh Token (Đăng xuất khỏi mọi thiết bị khác)
        authTokenRepository.revokeAllUserRefreshTokens(currentUser.getId());
    }

    // ======================================================
    // 2. LẤY THÔNG TIN PROFILE (GET USER PROFILE)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        // 1. Lấy người dùng (từ token)
        User currentUser = getCurrentAuthenticatedUser();

        // 2. Lấy vai trò cấp Hệ thống
        List<String> systemRoles = userRoleRepository.findByUser_Id(currentUser.getId())
                .stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .collect(Collectors.toList());

        // 3. Lấy vai trò cấp Công ty
        List<CompanyMembershipDTO> companyRoles = companyMemberRepository.findByUser_Id(currentUser.getId())
                .stream()
                .map(cm -> new CompanyMembershipDTO(
                        cm.getCompany().getId(),
                        cm.getCompany().getName(),
                        cm.getRole().getRoleCode()
                ))
                .collect(Collectors.toList());

        // 4. Lấy vai trò cấp Không gian làm việc
        List<WorkspaceMembershipDTO> workspaceRoles = workspaceMemberRepository.findByUser_Id(currentUser.getId())
                .stream()
                .map(wm -> new WorkspaceMembershipDTO(
                        wm.getWorkspace().getId(),
                        wm.getWorkspace().getName(),
                        wm.getWorkspace().getCompany().getId(),
                        wm.getRole().getRoleCode()
                ))
                .collect(Collectors.toList());

        // 5. Lấy vai trò cấp Dự án
        List<ProjectMembershipDTO> projectRoles = projectMemberRepository.findByUser_Id(currentUser.getId())
                .stream()
                .map(pm -> new ProjectMembershipDTO(
                        pm.getProject().getId(),
                        pm.getProject().getName(),
                        pm.getProject().getWorkspace().getId(), // Lấy ID không gian cha
                        pm.getRole().getRoleCode()
                ))
                .collect(Collectors.toList());
        
        // 6. Xử lý đường dẫn Avatar (Nếu là đường dẫn cục bộ, chuyển sang đường dẫn API)
        String avatarUrlFromDb = currentUser.getAvatarUrl();
        String finalAvatarUrl = avatarUrlFromDb;

        if (avatarUrlFromDb != null && !avatarUrlFromDb.isBlank() && !avatarUrlFromDb.startsWith("http")) {
            // Nếu là đường dẫn cục bộ (ví dụ: /avatars/uuid.jpg), thêm prefix API để tải về
            finalAvatarUrl = "/api/files" + avatarUrlFromDb; // Giả sử FileController mapping /api/files/**
        }

        // 7. Xây dựng và trả về DTO
        return UserProfileResponse.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .avatarUrl(finalAvatarUrl) // Sử dụng URL đã xử lý
                .phoneNumber(currentUser.getPhoneNumber())
                .dateOfBirth(currentUser.getDateOfBirth())
                .gender(currentUser.getGender())
                .status(currentUser.getStatus())
                .isEmailVerified(currentUser.getIsEmailVerified())
                .createdAt(currentUser.getCreatedAt())
                .lastLoginAt(currentUser.getLastLoginAt())
                .systemRoles(systemRoles)
                .companyMemberships(companyRoles)
                .workspaceMemberships(workspaceRoles)
                .projectMemberships(projectRoles)
                .build();
    }


    // ======================================================
    // 3. CẬP NHẬT THÔNG TIN CÁ NHÂN (UPDATE USER PROFILE)
    // ======================================================
    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(UpdateProfileRequest request, MultipartFile avatarFile) {
        // 1. Lấy người dùng hiện tại
        User currentUser = getCurrentAuthenticatedUser();

        // 2. Cập nhật các trường văn bản
        if (request.getFullName() != null) currentUser.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) currentUser.setPhoneNumber(request.getPhoneNumber());
        if (request.getDateOfBirth() != null) currentUser.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) currentUser.setGender(request.getGender());

        // 3. Xử lý Upload Ảnh Avatar
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Lưu vào thư mục "avatars"
            String avatarPath = fileStorageService.storeFile(avatarFile, "avatars");

            // Cập nhật đường dẫn vào Entity User
            currentUser.setAvatarUrl(avatarPath);
        }
        // Nếu request.getAvatarUrl() có giá trị (link ngoài hoặc muốn set rỗng/null)
        else if (request.getAvatarUrl() != null) {
             // Cho phép cập nhật link ảnh từ nguồn khác hoặc set null
             currentUser.setAvatarUrl(request.getAvatarUrl());
        }

        // 4. Lưu và trả về
        userRepository.save(currentUser);
        // Tái sử dụng hàm lấy profile để có đầy đủ thông tin membership
        return getCurrentUserProfile();
    }


    // ======================================================
    // ⚙️ PRIVATE HELPER: LẤY THÔNG TIN AUTHENTICATED USER
    // ======================================================
    /**
     * Helper: Lấy thông tin người dùng đã xác thực từ SecurityContext.
     */
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Kiểm tra xem có thông tin xác thực không
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Authenticated user information not found.");
        }
        
        // Lấy email (principal name) từ Authentication
        String email = authentication.getName();
        
        // Tìm User trong DB
        return userRepository.findByEmail(email)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}