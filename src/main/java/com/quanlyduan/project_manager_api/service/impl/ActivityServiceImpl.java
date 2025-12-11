package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.ActivityLogResponse;
import com.quanlyduan.project_manager_api.exception.AccessDeniedException;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.ActivityLog; // Đảm bảo import đúng Exception tùy chỉnh của bạn hoặc của Spring Security
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.ActivityLogRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.util.TimeUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SecurityService securityService;

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getLogsByContext(String scope, Integer id, int page, int size) {
        
        Integer currentUserId = securityService.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        List<ActivityLog> logs;

        switch (scope.toUpperCase()) {
            case "COMPANY":
                // 1. Check quyền Member
                if (!companyMemberRepository.existsByCompany_IdAndUser_Id(id, currentUserId)) {
                    throw new AccessDeniedException("You are not a member of this company.");
                }
                
                // 2. Query DB
                logs = activityLogRepository.findByCompanyIdOrderByCreatedAtDesc(id, pageable);

                // 3. 🔥 LỌC: Ẩn log INVITE và RECEIVED_INVITE khỏi bảng tin chung
                // Chỉ hiện: CREATE, UPDATE, DELETE, JOIN, REMOVE...
                logs = logs.stream()
                        .filter(log -> !log.getAction().equals("INVITE") 
                                    && !log.getAction().equals("RECEIVED_INVITE"))
                        .collect(Collectors.toList());
                break;
                
            case "WORKSPACE":
                // 1. Check quyền
                if (!workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(id, currentUserId)) {
                     throw new AccessDeniedException("You are not a member of this workspace.");
                }
                // 2. Query DB
                logs = activityLogRepository.findByWorkspaceIdOrderByCreatedAtDesc(id, pageable);

                // 3. 🔥 LỌC: Tương tự Company
                logs = logs.stream()
                        .filter(log -> !log.getAction().equals("INVITE") 
                                    && !log.getAction().equals("RECEIVED_INVITE"))
                        .collect(Collectors.toList());
                break;
                
            case "PROJECT":
                if (!projectMemberRepository.existsByProject_IdAndUser_Id(id, currentUserId)) {
                    throw new AccessDeniedException("You are not a member of this project.");
                }
                logs = activityLogRepository.findByProjectIdOrderByCreatedAtDesc(id, pageable);
                // Với Project, có thể bạn muốn giữ lại Invite để team lead theo dõi, 
                // hoặc lọc bỏ tùy ý. Ở đây tôi lọc bỏ cho đồng bộ.
                logs = logs.stream()
                        .filter(log -> !log.getAction().equals("INVITE") 
                                    && !log.getAction().equals("RECEIVED_INVITE"))
                        .collect(Collectors.toList());
                break;
                
            case "USER":
                if (!currentUserId.equals(id)) {
                    throw new AccessDeniedException("You can only view your own activity logs.");
                }
                // User xem log của chính mình thì CẦN THẤY RECEIVED_INVITE (để biết mình được mời)
                // và INVITE (để biết mình đã mời ai) -> KHÔNG LỌC
                logs = activityLogRepository.findByUserIdOrderByCreatedAtDesc(id, pageable);
                break;

            default:
                throw new BadRequestException("Invalid scope: " + scope);
        }

        return logs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }


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
                .entityName(log.getEntityName())
                .entityCode(log.getEntityCode())
                .description(log.getNewValue())
                .timestamp(log.getCreatedAt())
                .timeAgo(TimeUtils.getRelativeTimeAgo(log.getCreatedAt()))
                .build();
    }
}