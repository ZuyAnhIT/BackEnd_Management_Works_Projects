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
import com.quanlyduan.project_manager_api.model.ActivityLog;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.ActivityLogRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.util.TimeUtils;

@Service
public class ActivityServiceImpl {

    // Khai bao cac hang so de loai bo hardcode
    public static final String SCOPE_COMPANY = "COMPANY";
    public static final String SCOPE_WORKSPACE = "WORKSPACE";
    public static final String SCOPE_PROJECT = "PROJECT";
    public static final String SCOPE_USER = "USER";

    public static final String ACTION_INVITE = "INVITE";
    public static final String ACTION_RECEIVED_INVITE = "RECEIVED_INVITE";

    public static final String ERROR_NOT_COMPANY_MEMBER = "You are not a member of this company.";
    public static final String ERROR_NOT_WORKSPACE_MEMBER = "You are not a member of this workspace.";
    public static final String ERROR_NOT_PROJECT_MEMBER = "You are not a member of this project.";
    public static final String ERROR_VIEW_OWN_LOGS_ONLY = "You can only view your own activity logs.";
    public static final String ERROR_INVALID_SCOPE = "Invalid scope: ";

    public static final String DEFAULT_UNKNOWN_USER = "Unknown";
    public static final String DEFAULT_EMPTY_STRING = "";

    // Khai bao cac bien phu thuoc
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SecurityService securityService;

    // Constructor khoi tao thu cong
    public ActivityServiceImpl(ActivityLogRepository activityLogRepository,
                               UserRepository userRepository,
                               CompanyMemberRepository companyMemberRepository,
                               WorkspaceMemberRepository workspaceMemberRepository,
                               ProjectMemberRepository projectMemberRepository,
                               SecurityService securityService) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.securityService = securityService;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getLogsByContext(String scope, Integer id, int page, int size) {
        // Lay thong tin nguoi dung hien tai de phan quyen
        Integer currentUserId = securityService.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        List<ActivityLog> logs;

        // Kiem tra loai pham vi de xac thuc va phan luong truy van
        switch (scope.toUpperCase()) {
            case SCOPE_COMPANY:
                if (!companyMemberRepository.existsByCompany_IdAndUser_Id(id, currentUserId)) {
                    throw new AccessDeniedException(ERROR_NOT_COMPANY_MEMBER);
                }
                logs = activityLogRepository.findByCompanyIdOrderByCreatedAtDesc(id, pageable);
                logs = filterInvitationLogs(logs);
                break;
                
            case SCOPE_WORKSPACE:
                if (!workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(id, currentUserId)) {
                     throw new AccessDeniedException(ERROR_NOT_WORKSPACE_MEMBER);
                }
                logs = activityLogRepository.findByWorkspaceIdOrderByCreatedAtDesc(id, pageable);
                logs = filterInvitationLogs(logs);
                break;
                
            case SCOPE_PROJECT:
                if (!projectMemberRepository.existsByProject_IdAndUser_Id(id, currentUserId)) {
                    throw new AccessDeniedException(ERROR_NOT_PROJECT_MEMBER);
                }
                logs = activityLogRepository.findByProjectIdOrderByCreatedAtDesc(id, pageable);
                logs = filterInvitationLogs(logs);
                break;
                
            case SCOPE_USER:
                if (!currentUserId.equals(id)) {
                    throw new AccessDeniedException(ERROR_VIEW_OWN_LOGS_ONLY);
                }
                logs = activityLogRepository.findByUserIdOrderByCreatedAtDesc(id, pageable);
                break;

            default:
                throw new BadRequestException(ERROR_INVALID_SCOPE + scope);
        }

        // Chuyen doi danh sach thuc the sang DTO truoc khi tra ve
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- CAC HAM PRIVATE HO TRO (HELPERS) ---
    
    private List<ActivityLog> filterInvitationLogs(List<ActivityLog> logs) {
        return logs.stream()
                .filter(log -> !ACTION_INVITE.equals(log.getAction()) 
                            && !ACTION_RECEIVED_INVITE.equals(log.getAction()))
                .collect(Collectors.toList());
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private ActivityLogResponse mapToResponse(ActivityLog log) {
        User user = userRepository.findById(log.getUserId()).orElse(null);
        String userName = (user != null) ? user.getFullName() : DEFAULT_UNKNOWN_USER;
        String userAvatar = (user != null) ? user.getAvatarUrl() : DEFAULT_EMPTY_STRING;

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