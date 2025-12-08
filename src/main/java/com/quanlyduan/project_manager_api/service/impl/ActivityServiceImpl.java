package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.ActivityLogResponse;
import com.quanlyduan.project_manager_api.model.ActivityLog;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.ActivityLogRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.exception.AccessDeniedException; // Đảm bảo import đúng Exception tùy chỉnh của bạn hoặc của Spring Security
import com.quanlyduan.project_manager_api.exception.BadRequestException;

import com.quanlyduan.project_manager_api.util.TimeUtils; 
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SecurityService securityService;

    // Helper map Entity -> DTO
    private ActivityLogResponse mapToResponse(ActivityLog log) {
        User user = userRepository.findById(log.getUserId()).orElse(null);
        String userName = (user != null) ? user.getFullName() : "Unknown";
        String userAvatar = (user != null) ? user.getAvatarUrl() : "";

        return ActivityLogResponse.builder()
                .id(log.getId())
                .userName(userName)
                .userAvatar(userAvatar)
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getNewValue())
                .timestamp(log.getCreatedAt())
                .timeAgo(TimeUtils.getRelativeTimeAgo(log.getCreatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getLogsByContext(String scope, Integer id, int page, int size) {
        
        // 1. Lấy User hiện tại
        Integer currentUserId = securityService.getCurrentUserId();
        
        // 2. Chuẩn bị Pageable
        Pageable pageable = PageRequest.of(page, size);
        
        // 3. Khai báo biến logs
        List<ActivityLog> logs;

        // 4. KIỂM TRA QUYỀN VÀ QUERY DB TRONG TỪNG CASE
        switch (scope.toUpperCase()) {
            case "COMPANY":
                // Check quyền
                boolean isCompanyMember = companyMemberRepository.existsByCompany_IdAndUser_Id(id, currentUserId);
                if (!isCompanyMember) {
                    throw new AccessDeniedException("You are not a member of this company.");
                }
                // Query DB (Gán giá trị cho logs)
                logs = activityLogRepository.findByCompanyIdOrderByCreatedAtDesc(id, pageable);
                break;
                
            case "WORKSPACE":
                // Check quyền
                boolean isWorkspaceMember = workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(id, currentUserId);
                // (Có thể mở rộng logic: Nếu là Company Admin thì vẫn được xem, tùy nghiệp vụ)
                if (!isWorkspaceMember) {
                     throw new AccessDeniedException("You are not a member of this workspace.");
                }
                // Query DB
                logs = activityLogRepository.findByWorkspaceIdOrderByCreatedAtDesc(id, pageable);
                break;
                
            case "PROJECT":
                // Check quyền
                boolean isProjectMember = projectMemberRepository.existsByProject_IdAndUser_Id(id, currentUserId);
                if (!isProjectMember) {
                    throw new AccessDeniedException("You are not a member of this project.");
                }
                // Query DB
                logs = activityLogRepository.findByProjectIdOrderByCreatedAtDesc(id, pageable);
                break;
                
            case "USER":
                // Check quyền (Chỉ xem log của chính mình)
                if (!currentUserId.equals(id)) {
                    throw new AccessDeniedException("You can only view your own activity logs.");
                }
                // Query DB
                logs = activityLogRepository.findByUserIdOrderByCreatedAtDesc(id, pageable);
                break;

            default:
                throw new BadRequestException("Invalid scope: " + scope);
        }

        // 5. Map kết quả và trả về
        // Lúc này biến 'logs' chắc chắn đã được gán giá trị ở một trong các case trên
        return logs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
}