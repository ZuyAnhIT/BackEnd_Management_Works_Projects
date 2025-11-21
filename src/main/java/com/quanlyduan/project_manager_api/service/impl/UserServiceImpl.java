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
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository; // Đã dịch
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository; // Đã dịch
import com.quanlyduan.project_manager_api.repository.UserRepository; // Đã dịch
import com.quanlyduan.project_manager_api.repository.UserRoleRepository; // Đã dịch
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


    // LOGIC THAY DOI MAT KHAU
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 1. Lấy thông tin người dùng đang đăng nhập
        User currentUser = getCurrentAuthenticatedUser();

        // 2. Validate mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Incorrect old password");
        }

        // 3. Validate mật khẩu mới
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BadRequestException("New password must be different from the old password");
        }

        // 4. Validate mật khẩu xác nhận
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("Password confirmation does not match");
        }

        // 5. Hash và cập nhật mật khẩu mới
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 6. Lưu vào CSDL
        userRepository.save(currentUser);

        // 7.Thu hồi tất cả Refresh Token 
        // Đây là bước quan trọng để bảo mật. Khi đổi mật khẩu,
        // Tất cả các phiên đăng nhập ở thiết bị khác sẽ bị buộc đăng xuất.
        authTokenRepository.revokeAllUserRefreshTokens(currentUser.getId());
    }

    // --- Private Helper Method ---

    // LOGIC LAY THONG TIN DAY DU
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

        // 4. Lấy vai trò cấp Không gian
        List<WorkspaceMembershipDTO> workspaceRoles = workspaceMemberRepository.findByUser_Id(currentUser.getId()) // Đã dịch
                .stream()
                .map(wm -> new WorkspaceMembershipDTO( 
                        wm.getWorkspace().getId(), 
                        wm.getWorkspace().getName(), 
                        wm.getWorkspace().getCompany().getId(), 
                        wm.getRole().getRoleCode() 
                ))
                .collect(Collectors.toList());
        
        String avatarUrlFromDb = currentUser.getAvatarUrl();
        String finalAvatarUrl = avatarUrlFromDb; 

        if (avatarUrlFromDb != null && !avatarUrlFromDb.isBlank() && !avatarUrlFromDb.startsWith("http")) {
            // If the URL is NOT an internet link, it's a local file.
            // Build a URL that points to our new FileController.
            finalAvatarUrl = "/api/files/" + avatarUrlFromDb;
        }
       
        // 5. Xây dựng và trả về DTO
    List<ProjectMembershipDTO> projectRoles = projectMemberRepository.findByUser_Id(currentUser.getId())
            .stream()
            .map(pm -> new ProjectMembershipDTO(
                    pm.getProject().getId(),
                    pm.getProject().getName(),
                    pm.getProject().getWorkspace().getId(), // Lấy ID không gian cha
                    pm.getRole().getRoleCode()
            ))
            .collect(Collectors.toList());

        // 6. Xây dựng và trả về DTO (Đã cập nhật)
        return UserProfileResponse.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .avatarUrl(finalAvatarUrl) 
                .phoneNumber(currentUser.getPhoneNumber())
                .dateOfBirth(currentUser.getDateOfBirth())
                .gender(currentUser.getGender())
                .status(currentUser.getStatus())
                .isEmailVerified(currentUser.getIsEmailVerified())
                .createdAt(currentUser.getCreatedAt())
                .lastLoginAt(currentUser.getLastLoginAt())
                .avatarUrl(currentUser.getAvatarUrl())
                .systemRoles(systemRoles)
                .companyMemberships(companyRoles)
                .workspaceMemberships(workspaceRoles)
                .projectMemberships(projectRoles) 
                .build();
    }


    // LOGIC CAP NHAT THONG TIN CA NHAN (TICH HOP UPLOAD ANH)
    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(UpdateProfileRequest request, MultipartFile avatarFile) {
        // 1. Lấy người dùng hiện tại
        User currentUser = getCurrentAuthenticatedUser();

        // 2. Cập nhật các trường văn bản (như cũ)
        if (request.getFullName() != null) currentUser.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) currentUser.setPhoneNumber(request.getPhoneNumber());
        if (request.getDateOfBirth() != null) currentUser.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) currentUser.setGender(request.getGender());

        // 3. Xử lý Upload Ảnh (MỚI)
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Lưu vào thư mục "avatars"
            String avatarPath = fileStorageService.storeFile(avatarFile, "avatars");
            
            // Cập nhật đường dẫn vào Entity User
            // (Lưu ý: Bạn có thể cần thêm logic xóa ảnh cũ nếu muốn tiết kiệm dung lượng)
            currentUser.setAvatarUrl(avatarPath);
        }
        // Nếu request.getAvatarUrl() có giá trị string (link ngoài) và avatarFile null, 
        // bạn cũng có thể cho phép cập nhật link ảnh từ nguồn khác.
        else if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
             currentUser.setAvatarUrl(request.getAvatarUrl());
        }

        // 4. Lưu và trả về
        userRepository.save(currentUser);
        return getCurrentUserProfile();
    }


    // LOGIC LAY NGUOI DUNG HIEN TAI
    private User getCurrentAuthenticatedUser() { // Đã dịch
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadRequestException("Không tìm thấy thông tin người dùng đã xác thực."); // Đã dịch
        }
        
        String email = authentication.getName();
        return userRepository.findByEmail(email) // Đã dịch
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email)); // Đã dịch
    }


}
