package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.AdminUserResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.AdminUserService;

@Service
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SecurityService securityService;

    // Cập nhật Constructor để Inject thêm các dependency cần thiết
    public AdminUserServiceImpl(UserRepository userRepository, 
                                RoleRepository roleRepository, 
                                SecurityService securityService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.securityService = securityService;
    }

    // ======================================================
    // 1. TÌM KIẾM & LẤY DANH SÁCH (READ)
    // ======================================================
    
    @Override
    public PageResponseDTO<AdminUserResponse> searchUsers(
            String keyword, String status, int page, int size, String sortBy, String sortDir) {

        String safeKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String safeStatusStr = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        com.quanlyduan.project_manager_api.model.common.enums.UserStatus enumStatus = null;
        if (safeStatusStr != null) {
            try {
                enumStatus = com.quanlyduan.project_manager_api.model.common.enums.UserStatus.valueOf(safeStatusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                enumStatus = null; 
            }
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                    ? Sort.by(sortBy).ascending() 
                    : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage = userRepository.searchGlobalUsers(safeKeyword, enumStatus, pageable);

        List<AdminUserResponse> content = userPage.getContent().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());

        return PageResponseDTO.<AdminUserResponse>builder()
                .content(content)
                .pageNumber(userPage.getNumber()) 
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    // ======================================================
    // 2. CÁC HÀNH ĐỘNG QUẢN TRỊ (ACTIONS)
    // ======================================================

    @Override
    @Transactional
    public void changeUserStatus(Integer userId, String newStatusStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        com.quanlyduan.project_manager_api.model.common.enums.UserStatus newStatus;
        try {
            newStatus = com.quanlyduan.project_manager_api.model.common.enums.UserStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid user status format.");
        }

        // Bảo mật: Không cho phép Admin tự khóa tài khoản của chính mình
        User currentUser = securityService.getCurrentAuthenticatedUser();
        if (currentUser.getId().equals(userId) && newStatus == com.quanlyduan.project_manager_api.model.common.enums.UserStatus.LOCKED) {
            throw new BadRequestException("Action denied: You cannot ban your own account.");
        }

        user.setStatus(newStatus);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleSystemAdminRole(Integer userId, boolean isAssign) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Role sysAdminRole = roleRepository.findByRoleCode("SYSTEM_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Critical Error: Role SYSTEM_ADMIN not found in database."));

        if (isAssign) {
            // Gán quyền
            user.getRoles().add(sysAdminRole);
        } else {
            // Thu hồi quyền
            User currentUser = securityService.getCurrentAuthenticatedUser();
            // Bảo mật: Không cho phép Admin tự tước quyền của chính mình
            if (currentUser.getId().equals(userId)) {
                throw new BadRequestException("Action denied: You cannot revoke your own SYSTEM_ADMIN privileges.");
            }
            user.getRoles().remove(sysAdminRole);
        }

        userRepository.save(user);
    }

    // ======================================================
    // 3. MAPPING HELPERS
    // ======================================================

    private AdminUserResponse mapToAdminUserResponse(User user) {
        // Đã mở khóa: Lấy danh sách tên quyền thực tế từ Entity thay vì để trống
        List<String> roleNames = user.getRoles() != null 
                ? user.getRoles().stream().map(Role::getRoleCode).collect(Collectors.toList())
                : java.util.Collections.emptyList();

        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus() != null ? user.getStatus().name() : "UNKNOWN")
                .avatarUrl(user.getAvatarUrl())
                .roles(roleNames) 
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}