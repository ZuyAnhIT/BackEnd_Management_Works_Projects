package com.quanlyduan.project_manager_api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProfileRequest;
import com.quanlyduan.project_manager_api.dto.response.CompanyMembershipDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectMembershipDTO;
import com.quanlyduan.project_manager_api.dto.response.UserProfileResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMembershipDTO;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.AuthTokenRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.UserRoleRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.service.FileStorageService;
import com.quanlyduan.project_manager_api.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ERROR_INCORRECT_PASSWORD = "Incorrect old password.";
    public static final String ERROR_SAME_PASSWORD = "New password must be different from the old password.";
    public static final String ERROR_PASSWORD_MISMATCH = "Password confirmation does not match.";
    public static final String ERROR_AUTH_NOT_FOUND = "Authenticated user information not found.";
    public static final String ERROR_USER_NOT_FOUND_EMAIL = "User not found with email: ";

    public static final String ROLE_GUEST = "GUEST";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String ANONYMOUS_USER = "anonymousUser";

    public static final String AVATAR_PREFIX_HTTP = "http";
    public static final String AVATAR_API_PATH = "/api/files";
    public static final String FOLDER_AVATARS = "avatars";

    // Khai bao cac bien phu thuoc
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AuthTokenRepository authTokenRepository;
    private final FileStorageService fileStorageService;

    // Constructor khoi tao thu cong thay the cho @RequiredArgsConstructor
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder, 
                           UserRoleRepository userRoleRepository, 
                           CompanyMemberRepository companyMemberRepository, 
                           WorkspaceMemberRepository workspaceMemberRepository, 
                           ProjectMemberRepository projectMemberRepository, 
                           AuthTokenRepository authTokenRepository, 
                           FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository = userRoleRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.authTokenRepository = authTokenRepository;
        this.fileStorageService = fileStorageService;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // Lay thong tin nguoi dung hien tai tu nguyen canh bao mat
        User currentUser = getCurrentAuthenticatedUser();

        // Xac thuc mat khau cu
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new BadRequestException(ERROR_INCORRECT_PASSWORD);
        }

        // Xac thuc mat khau moi khong duoc trung voi mat khau cu
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BadRequestException(ERROR_SAME_PASSWORD);
        }

        // Kiem tra mat khau moi va xac nhan mat khau phai trung khop
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException(ERROR_PASSWORD_MISMATCH);
        }

        // Ma hoa va cap nhat mat khau moi vao co so du lieu
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        // Thu hoi toan bo phien dang nhap truoc do de dam bao an toan
        authTokenRepository.revokeAllUserRefreshTokens(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        // Lay thong tin co ban cua nguoi dung
        User currentUser = getCurrentAuthenticatedUser();

        // Truy xuat cac vai tro cap he thong cua nguoi dung
        List<String> systemRoles = userRoleRepository.findByUser_Id(currentUser.getId())
                .stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .collect(Collectors.toList());

        // Truy xuat cac vai tro cap cong ty
        List<CompanyMembershipDTO> companyRoles = new ArrayList<>(
                companyMemberRepository.findByUser_Id(currentUser.getId())
                        .stream()
                        .map(cm -> new CompanyMembershipDTO(
                                cm.getCompany().getId(),
                                cm.getCompany().getName(),
                                cm.getRole().getRoleCode(),
                                cm.getCompany().getStatus() != null ? cm.getCompany().getStatus().toString() : STATUS_ACTIVE
                        ))
                        .collect(Collectors.toList())
        );

        // Truy xuat cac vai tro cap khong gian lam viec
        List<WorkspaceMembershipDTO> workspaceRoles = new ArrayList<>(
                workspaceMemberRepository.findByUser_Id(currentUser.getId())
                        .stream()
                        .map(wm -> new WorkspaceMembershipDTO(
                                wm.getWorkspace().getId(),
                                wm.getWorkspace().getName(),
                                wm.getWorkspace().getCompany().getId(),
                                wm.getRole().getRoleCode()
                        ))
                        .collect(Collectors.toList())
        );

        // Truy xuat danh sach thuc the thanh vien du an
        List<ProjectMember> projectMembers = projectMemberRepository.findByUser_Id(currentUser.getId());

        // Chuyen doi thuc the thanh vien du an sang DTO
        List<ProjectMembershipDTO> projectRoles = projectMembers.stream()
                .map(pm -> new ProjectMembershipDTO(
                        pm.getProject().getId(),
                        pm.getProject().getName(),
                        pm.getProject().getWorkspace().getId(),
                        pm.getRole().getRoleCode()
                ))
                .collect(Collectors.toList());

        // Suy dien va bo sung vai tro GUEST cho nguoi dung o cap cao hon neu ho chi la thanh vien o cap du an
        Set<Integer> existingCompanyIds = companyRoles.stream()
                .map(CompanyMembershipDTO::getCompanyId)
                .collect(Collectors.toSet());

        Set<Integer> existingWorkspaceIds = workspaceRoles.stream()
                .map(WorkspaceMembershipDTO::getWorkspaceId)
                .collect(Collectors.toSet());

        for (ProjectMember pm : projectMembers) {
            var project = pm.getProject();
            var workspace = project.getWorkspace();
            var company = workspace.getCompany();

            // Bo sung khong gian lam viec thieu voi vai tro GUEST
            if (!existingWorkspaceIds.contains(workspace.getId())) {
                workspaceRoles.add(new WorkspaceMembershipDTO(
                        workspace.getId(),
                        workspace.getName(),
                        company.getId(),
                        ROLE_GUEST
                ));
                existingWorkspaceIds.add(workspace.getId()); 
            }

            // Bo sung cong ty thieu voi vai tro GUEST
            if (!existingCompanyIds.contains(company.getId())) {
                companyRoles.add(new CompanyMembershipDTO(
                        company.getId(),
                        company.getName(),
                        ROLE_GUEST,
                        company.getStatus() != null ? company.getStatus().toString() : STATUS_ACTIVE
                ));
                existingCompanyIds.add(company.getId()); 
            }
        }

        // Xu ly duong dan hinh anh dai dien de phuc vu viec hien thi
        String avatarUrlFromDb = currentUser.getAvatarUrl();
        String finalAvatarUrl = avatarUrlFromDb;

        if (avatarUrlFromDb != null && !avatarUrlFromDb.isBlank() && !avatarUrlFromDb.startsWith(AVATAR_PREFIX_HTTP)) {
            finalAvatarUrl = AVATAR_API_PATH + avatarUrlFromDb;
        }

        // Dong goi toan bo thong tin vao DTO phan hoi
        return buildUserProfileResponse(currentUser, finalAvatarUrl, systemRoles, companyRoles, workspaceRoles, projectRoles);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(UpdateProfileRequest request, MultipartFile avatarFile) {
        // Lay thong tin nguoi dung hien tai
        User currentUser = getCurrentAuthenticatedUser();

        // Cap nhat cac truong thong tin ca nhan
        if (request.getFullName() != null) {
            currentUser.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            currentUser.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            currentUser.setGender(request.getGender());
        }

        // Xu ly tai len va luu tru hinh dai dien
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarPath = fileStorageService.storeFile(avatarFile, FOLDER_AVATARS);
            currentUser.setAvatarUrl(avatarPath);
        } 
        else if (request.getAvatarUrl() != null) {
            currentUser.setAvatarUrl(request.getAvatarUrl());
        }

        // Luu thong tin cap nhat vao co so du lieu
        userRepository.save(currentUser);
        
        // Tra ve thong tin ho so moi nhat kem theo cac vai tro
        return getCurrentUserProfile();
    }

    // --- CAC HAM PRIVATE HO TRO NGHIEP VU ---

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Kiem tra nguyen canh bao mat co chua thong tin xac thuc hop le khong
        if (authentication == null || !authentication.isAuthenticated() || ANONYMOUS_USER.equals(authentication.getPrincipal())) {
            throw new BadRequestException(ERROR_AUTH_NOT_FOUND);
        }
        
        String email = authentication.getName();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(ERROR_USER_NOT_FOUND_EMAIL + email));
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private UserProfileResponse buildUserProfileResponse(User currentUser, String finalAvatarUrl, 
                                                         List<String> systemRoles, 
                                                         List<CompanyMembershipDTO> companyRoles, 
                                                         List<WorkspaceMembershipDTO> workspaceRoles, 
                                                         List<ProjectMembershipDTO> projectRoles) {
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
                .systemRoles(systemRoles)
                .companyMemberships(companyRoles)     
                .workspaceMemberships(workspaceRoles) 
                .projectMemberships(projectRoles)
                .build();
    }
}