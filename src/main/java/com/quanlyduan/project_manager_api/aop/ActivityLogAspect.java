package com.quanlyduan.project_manager_api.aop;

import com.quanlyduan.project_manager_api.dto.response.*; // Import * cho gọn
import com.quanlyduan.project_manager_api.model.ActivityLog;
import com.quanlyduan.project_manager_api.model.CompanyInvitation;
import com.quanlyduan.project_manager_api.model.ProjectInvitation;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.ActivityLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Optional;

@Aspect
@Component
@Slf4j
public class ActivityLogAspect {

    private final ActivityLogService activityLogService;
    private final SecurityService securityService;
    private final UserRepository userRepository;

    public ActivityLogAspect(ActivityLogService activityLogService, SecurityService securityService, UserRepository userRepository){
        this.activityLogService = activityLogService;
        this.securityService = securityService;
        this.userRepository = userRepository;
    }

    @AfterReturning(pointcut = "@annotation(logActivity)", returning = "result")
    public void logAfterMethod(JoinPoint joinPoint, LogActivity logActivity, Object result) {
        try {
            Integer userId = 0;
            try { userId = securityService.getCurrentUserId(); } catch (Exception e) {}

            String ipAddress = "Unknown";
            String userAgent = "Unknown";
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                if (request != null) {
                    ipAddress = request.getRemoteAddr();
                    userAgent = request.getHeader("User-Agent");
                }
            } catch (Exception ignored) {}

            Integer entityId = extractId(result);
            ContextIds contextIds = extractContextIds(result);
            
            // 1. LOG CHÍNH (Người gửi)
            String description = ActivityLogContext.getDetail();
            if (description == null || description.isEmpty()) {
                description = generateDefaultDescription(logActivity, contextIds);
            }

            ActivityLog logEntity = ActivityLog.builder()
                    .userId(userId)
                    .action(logActivity.action())
                    .entityType(logActivity.entityType())
                    .entityId(entityId)
                    .companyId(contextIds.companyId)
                    .workspaceId(contextIds.workspaceId)
                    .projectId(contextIds.projectId)
                    .newValue(description)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            activityLogService.saveLog(logEntity);

            // 2. LOG PHỤ (Người nhận) - NẾU LÀ INVITE
            if ("INVITE".equalsIgnoreCase(logActivity.action()) && contextIds.entityName != null) {
                // entityName lúc này là Email người nhận
                createLogForRecipient(contextIds.entityName, logActivity, entityId, contextIds, ipAddress, userAgent);
            }
            
            ActivityLogContext.clear();

        } catch (Exception e) {
            log.error("Error saving activity log: {}", e.getMessage());
            ActivityLogContext.clear();
        }
    }

    private static class ContextIds {
        Integer companyId;
        Integer workspaceId;
        Integer projectId;
        String entityName;
        String entityCode;
        String targetName;
    }

    /**
     * Hàm tạo log riêng cho người được mời
     */
    private void createLogForRecipient(String email, LogActivity logActivity, Integer entityId, ContextIds contextIds, String ip, String ua) {
        try {
            Optional<User> recipientOpt = userRepository.findByEmail(email);
            
            if (recipientOpt.isPresent()) {
                User recipient = recipientOpt.get();
                
                // Xác định loại mời: "company", "project", "workspace"
                String inviteType = logActivity.entityType().toLowerCase().replace("_member", "");
                
                // Lấy tên đích (TechVision, E-Commerce App...)
                String targetName = (contextIds.targetName != null) ? contextIds.targetName : "the system";

                // Tạo thông báo chi tiết
                // VD: You have been invited to company <strong>TechVision Solutions</strong>.
                String recipientMsg = String.format("You have been invited to join %s <strong>%s</strong>. Please check your email.", 
                        inviteType, targetName);

                ActivityLog recipientLog = ActivityLog.builder()
                        .userId(recipient.getId()) 
                        .action("RECEIVED_INVITE") 
                        .entityType(logActivity.entityType())
                        .entityId(entityId) // Lưu ID lời mời để Frontend xử lý nút Accept
                        .companyId(contextIds.companyId)
                        .workspaceId(contextIds.workspaceId)
                        .projectId(contextIds.projectId)
                        .newValue(recipientMsg)
                        .ipAddress(ip)
                        .userAgent(ua)
                        .build();

                activityLogService.saveLog(recipientLog);
            }
        } catch (Exception ex) {
            log.warn("Could not create recipient log: " + ex.getMessage());
        }
    }

    private ContextIds extractContextIds(Object result) {
        ContextIds ids = new ContextIds();
        if (result == null) return ids;

        Object data = result;
        if (result instanceof com.quanlyduan.project_manager_api.dto.response.ApiResponse) {
            data = ((com.quanlyduan.project_manager_api.dto.response.ApiResponse<?>) result).getData();
        }
        
        if (data == null) return ids;

        try {
            // 1. TASK SUMMARY
            if (data instanceof TaskSummaryResponse) {
                TaskSummaryResponse t = (TaskSummaryResponse) data;
                ids.projectId = t.getProjectId();
                // ids.workspaceId = t.getWorkspaceId();
                // ids.companyId = t.getCompanyId();
                ids.entityName = t.getTitle();
                ids.entityCode = t.getTaskCode();
            }
            // 2. TASK DETAIL
            else if (data instanceof TaskResponse) {
                TaskResponse t = (TaskResponse) data;
                if (t.getProject() != null) ids.projectId = t.getProject().getId();
                // ids.workspaceId = t.getWorkspaceId();
                // ids.companyId = t.getCompanyId();
                ids.entityName = t.getTitle();
                ids.entityCode = t.getTaskCode();
            }
            // 3. PROJECT
            else if (data instanceof ProjectResponse) {
                ProjectResponse p = (ProjectResponse) data;
                ids.projectId = p.getId();
                // ids.workspaceId = p.getWorkspaceId();
                // ids.companyId = p.getCompanyId();
                ids.entityName = p.getName();
                ids.entityCode = p.getProjectCode();
            }
            // 4. WORKSPACE (SỬA LỖI LOGIC CŨ Ở ĐÂY)
            else if (data instanceof WorkspaceResponse) {
                WorkspaceResponse w = (WorkspaceResponse) data;
                ids.workspaceId = w.getWorkspaceId();
                // ids.companyId = w.getCompanyId();
                ids.entityName = w.getWorkspaceName();
            }
            // --- PROJECT STATUS ---
            else if (data instanceof ProjectStatusResponse) {
                ProjectStatusResponse s = (ProjectStatusResponse) data;
                ids.projectId = s.getProjectId();
                // Nếu bạn đã thêm workspaceId/companyId vào DTO này thì uncomment:
                // ids.workspaceId = s.getWorkspaceId();
                // ids.companyId = s.getCompanyId();
                ids.entityName = s.getName();
                ids.entityCode = "COLUMN";
            }
            // --- INVITATION (Xử lý Email người được mời) ---
            else if (data instanceof CompanyInvitation) {
                CompanyInvitation inv = (CompanyInvitation) data;
                ids.companyId = inv.getCompany().getId();
                ids.entityName = inv.getEmail(); // Hiển thị email người được mời
                ids.targetName = inv.getCompany().getName();
            }
            else if (data instanceof ProjectInvitation) {
                ProjectInvitation inv = (ProjectInvitation) data;
                ids.projectId = inv.getProject().getId();
                 if(inv.getProject().getWorkspace() != null && inv.getProject().getWorkspace().getCompany() != null) {
                    ids.companyId = inv.getProject().getWorkspace().getCompany().getId();
                }
                // ids.companyId = inv.getProject().getWorkspace().getCompany().getId();
                ids.entityName = inv.getEmail();
                ids.targetName = inv.getProject().getName();
            }
            // 5. COMMENT
            else if (data instanceof TaskCommentResponse) {
                TaskCommentResponse c = (TaskCommentResponse) data;
                ids.projectId = c.getProjectId();
                // ids.workspaceId = c.getWorkspaceId();
                // ids.companyId = c.getCompanyId();
                ids.entityName = "Comment on Task";
            }
            // 6. ATTACHMENT
            else if (data instanceof TaskAttachmentResponse) {
                TaskAttachmentResponse a = (TaskAttachmentResponse) data;
                ids.projectId = a.getProjectId();
                // ids.workspaceId = a.getWorkspaceId();
                // ids.companyId = a.getCompanyId();
                ids.entityName = a.getFileName();
            }
            // 7. SUBTASK
            else if (data instanceof SubTaskResponse) {
                SubTaskResponse s = (SubTaskResponse) data;
                ids.projectId = s.getProjectId();
                ids.entityName = s.getTitle();
                ids.entityCode = s.getId() != null ? "SUB-" + s.getId() : null;
            }
            // 8. EPIC
            else if (data instanceof EpicResponse) {
                EpicResponse e = (EpicResponse) data;
                ids.projectId = e.getProjectId();
                ids.entityName = e.getName();
                ids.entityCode = e.getEpicCode();
            }
            // 9. TAG
            else if (data instanceof TagResponse) {
                TagResponse t = (TagResponse) data;
                ids.projectId = t.getProjectId();
                ids.entityName = t.getName();
            }
            // 10. COMPANY
            else if (data instanceof CompanyDetailsResponse) {
                CompanyDetailsResponse c = (CompanyDetailsResponse) data;
                ids.companyId = c.getCompanyId();
                ids.entityName = c.getCompanyName();
                ids.entityCode = c.getCompanyCode();
            }
            // 11. SPRINT
            else if (data instanceof SprintResponse) {
                SprintResponse s = (SprintResponse) data;
                ids.projectId = s.getProjectId();
                ids.entityName = s.getName();
            }
        } catch (Exception e) {
            log.warn("Could not extract context IDs: " + e.getMessage());
        }
        return ids;
    }

    private Integer extractId(Object result) {
        Object data = unwrapResult(result);
        if (data == null) return null;

        try {
            // Ưu tiên tìm hàm getId()
            Method getIdMethod = data.getClass().getMethod("getId");
            Object idObj = getIdMethod.invoke(data);
            if (idObj instanceof Integer) return (Integer) idObj;
        } catch (Exception e) {
            return tryAlternativeIds(data);
        }
        return null;
    }

    private Integer tryAlternativeIds(Object data) {
        String[] possibleMethods = {"getTaskId", "getProjectId", "getWorkspaceId", "getCompanyId", "getCommentId"};
        for (String methodName : possibleMethods) {
            try {
                Method method = data.getClass().getMethod(methodName);
                Object val = method.invoke(data);
                if (val instanceof Integer) return (Integer) val;
            } catch (Exception ignored) {}
        }
        return null;
    }

    // Helper bóc tách ResponseEntity/ApiResponse
    private Object unwrapResult(Object result) {
        if (result == null) return null;
        Object data = result;
        if (result instanceof org.springframework.http.ResponseEntity) {
            data = ((org.springframework.http.ResponseEntity<?>) result).getBody();
        }
        if (data instanceof com.quanlyduan.project_manager_api.dto.response.ApiResponse) {
            data = ((com.quanlyduan.project_manager_api.dto.response.ApiResponse<?>) data).getData();
        }
        return data;
    }
     // Helper: Format phần đầu "create a task..."
    private String formatActionHeader(LogActivity logActivity) {
        String action = logActivity.action().toLowerCase().replace("_", " ");
        String type = logActivity.entityType().toLowerCase().replace("_", " ");
        return action + " a " + type;
    }

    // Helper: Tạo mô tả mặc định (khi Service không gửi chi tiết)
    private String generateDefaultDescription(LogActivity logActivity, ContextIds context) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatActionHeader(logActivity));

        if (context.entityCode != null) {
            sb.append(" <strong>").append(context.entityCode).append("</strong>");
            if (context.entityName != null) {
                sb.append(" - ").append(context.entityName);
            }
        } else if (context.entityName != null) {
            sb.append(" \"").append(context.entityName).append("\"");
        }
        return sb.toString();
    }
}
