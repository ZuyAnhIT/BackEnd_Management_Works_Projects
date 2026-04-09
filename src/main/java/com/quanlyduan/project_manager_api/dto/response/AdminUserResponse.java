package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserResponse {
    private Integer id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String status;           // ACTIVE, BANNED, UNVERIFIED
    private String avatarUrl;
    private List<String> roles;      // Danh sách các quyền (Ví dụ: ["SYSTEM_ADMIN", "USER"])
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt; // (Tùy chọn nếu DB của bạn có)
}