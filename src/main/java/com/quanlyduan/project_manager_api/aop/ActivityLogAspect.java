package com.quanlyduan.project_manager_api.aop;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.EpicResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectStatusResponse;
import com.quanlyduan.project_manager_api.dto.response.SprintResponse;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TagResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
import com.quanlyduan.project_manager_api.model.ActivityLog;
import com.quanlyduan.project_manager_api.model.CompanyInvitation;
import com.quanlyduan.project_manager_api.model.ProjectInvitation;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.WorkspaceMember;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.ActivityLogService;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class ActivityLogAspect {

    // Khai bao cac hang so
    public static final String UNKNOWN_VALUE = "Unknown";
    public static final String ACTION_INVITE = "INVITE";
    public static final String ACTION_RECEIVED_INVITE = "RECEIVED_INVITE";
    public static final String SCOPE_SYSTEM = "system";
    public static final String SCOPE_COMPANY = "company";
    public static final String SCOPE_WORKSPACE = "workspace";
    public static final String SCOPE_PROJECT = "project";
    public static final String DEFAULT_TARGET_NAME = "the system";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String ENTITY_CODE_COLUMN = "column";
    public static final String ENTITY_NAME_COMMENT = "Comment on Task";
    public static final String PREFIX_SUBTASK = "SUB-";
    
    public static final String MSG_INVITED_TEMPLATE = "You have been invited to join %s <strong>%s</strong>. Please check your email.";
    public static final String LOG_ERROR_SAVING = "Error saving activity log: {}";
    public static final String LOG_ERROR_RECIPIENT = "Could not create recipient log: {}";
    public static final String LOG_ERROR_EXTRACT_ID = "Could not extract context IDs: {}";

    private final ActivityLogService activityLogService;
    private final SecurityService securityService;
    private final UserRepository userRepository;

    // Constructor khoi tao thu cong
    public ActivityLogAspect(ActivityLogService activityLogService, 
                             SecurityService securityService, 
                             UserRepository userRepository) {
        this.activityLogService = activityLogService;
        this.securityService = securityService;
        this.userRepository = userRepository;
    }

    @AfterReturning(pointcut = "@annotation(logActivity)", returning = "result")
    public void logAfterMethod(JoinPoint joinPoint, LogActivity logActivity, Object result) {
        try {
            Integer userId = extractCurrentUserId();
            String ipAddress = extractClientIpAddress();
            String userAgent = extractClientUserAgent();

            Integer entityId = extractId(result);
            ContextIds contextIds = extractContextIds(result);
            
            String description = ActivityLogContext.getDetail();
            if (description == null || description.isEmpty()) {
                description = generateDefaultDescription(logActivity, contextIds);
            }

            // Ghi nhan thoi gian thuc thi de thoa man rang buoc NOT NULL cua Database
            LocalDateTime now = LocalDateTime.now();

            ActivityLog logEntity = ActivityLog.builder()
                    .userId(userId)
                    .action(logActivity.action())
                    .entityType(logActivity.entityType())
                    .entityId(entityId)
                    .entityName(contextIds.entityName)
                    .entityCode(contextIds.entityCode)
                    .companyId(contextIds.companyId)
                    .workspaceId(contextIds.workspaceId)
                    .projectId(contextIds.projectId)
                    .newValue(description)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .timestamp(now) 
                    .createdAt(now) 
                    .build();

            activityLogService.saveLog(logEntity);

            if (ACTION_INVITE.equalsIgnoreCase(logActivity.action()) && contextIds.entityName != null) {
                createLogForRecipient(contextIds.entityName, logActivity, entityId, contextIds, ipAddress, userAgent, now);
            }

        } catch (Exception e) {
            log.error(LOG_ERROR_SAVING, e.getMessage());
        } finally {
            ActivityLogContext.clear();
        }
    }

    // --- CAC HAM PRIVATE HO TRO (HELPERS) ---

    private Integer extractCurrentUserId() {
        try {
            return securityService.getCurrentUserId();
        } catch (Exception e) {
            return 0;
        }
    }

    private String extractClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null && attributes.getRequest() != null) {
                return attributes.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return UNKNOWN_VALUE;
    }

    private String extractClientUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null && attributes.getRequest() != null) {
                return attributes.getRequest().getHeader(HEADER_USER_AGENT);
            }
        } catch (Exception ignored) {}
        return UNKNOWN_VALUE;
    }

    private void createLogForRecipient(String email, LogActivity logActivity, Integer entityId, ContextIds contextIds, String ip, String ua, LocalDateTime now) {
        try {
            Optional<User> recipientOpt = userRepository.findByEmail(email);
            
            if (recipientOpt.isPresent()) {
                User recipient = recipientOpt.get();
                String entityTypeUpper = logActivity.entityType().toUpperCase();
                String scope = SCOPE_SYSTEM; 

                if (entityTypeUpper.contains("COMPANY")) {
                    scope = SCOPE_COMPANY;
                } else if (entityTypeUpper.contains("WORKSPACE")) {
                    scope = SCOPE_WORKSPACE;
                } else if (entityTypeUpper.contains("PROJECT")) {
                    scope = SCOPE_PROJECT;
                }

                String targetName = (contextIds.targetName != null) ? contextIds.targetName : DEFAULT_TARGET_NAME;
                String recipientMsg = String.format(MSG_INVITED_TEMPLATE, scope, targetName);

                ActivityLog recipientLog = ActivityLog.builder()
                        .userId(recipient.getId())
                        .action(ACTION_RECEIVED_INVITE)
                        .entityType(logActivity.entityType())
                        .entityId(entityId)
                        .companyId(contextIds.companyId)
                        .workspaceId(contextIds.workspaceId)
                        .projectId(contextIds.projectId)
                        .newValue(recipientMsg)
                        .ipAddress(ip)
                        .userAgent(ua)
                        .timestamp(now)
                        .createdAt(now)
                        .build();

                activityLogService.saveLog(recipientLog);
            }
        } catch (Exception ex) {
            log.warn(LOG_ERROR_RECIPIENT, ex.getMessage());
        }
    }

    private ContextIds extractContextIds(Object result) {
        ContextIds ids = new ContextIds();
        Object data = unwrapResult(result);
        
        if (data == null) {
            return ids;
        }

        try {
            if (data instanceof TaskSummaryResponse) {
                TaskSummaryResponse t = (TaskSummaryResponse) data;
                ids.projectId = t.getProjectId();
                ids.entityName = t.getTitle();
                ids.entityCode = t.getTaskCode();
            } else if (data instanceof TaskResponse) {
                TaskResponse t = (TaskResponse) data;
                if (t.getProject() != null) ids.projectId = t.getProject().getId();
                ids.entityName = t.getTitle();
                ids.entityCode = t.getTaskCode();
            } else if (data instanceof ProjectResponse) {
                ProjectResponse p = (ProjectResponse) data;
                ids.projectId = p.getId();
                ids.entityName = p.getName();
                ids.entityCode = p.getProjectCode();
            } else if (data instanceof WorkspaceResponse) {
                WorkspaceResponse w = (WorkspaceResponse) data;
                ids.workspaceId = w.getWorkspaceId();
                ids.entityName = w.getWorkspaceName();
            } else if (data instanceof ProjectStatusResponse) {
                ProjectStatusResponse s = (ProjectStatusResponse) data;
                ids.projectId = s.getProjectId();
                ids.entityName = s.getName();
                ids.entityCode = ENTITY_CODE_COLUMN;
            } else if (data instanceof WorkspaceMember) {
                WorkspaceMember wm = (WorkspaceMember) data;
                ids.workspaceId = wm.getWorkspace().getId();
                ids.entityName = wm.getUser().getEmail();
                ids.targetName = wm.getWorkspace().getName();
            } else if (data instanceof CompanyInvitation) {
                CompanyInvitation inv = (CompanyInvitation) data;
                ids.companyId = inv.getCompany().getId();
                ids.entityName = inv.getEmail();
                ids.targetName = inv.getCompany().getName();
            } else if (data instanceof ProjectInvitation) {
                ProjectInvitation inv = (ProjectInvitation) data;
                ids.projectId = inv.getProject().getId();
                 if(inv.getProject().getWorkspace() != null && inv.getProject().getWorkspace().getCompany() != null) {
                     ids.companyId = inv.getProject().getWorkspace().getCompany().getId();
                }
                ids.entityName = inv.getEmail();
                ids.targetName = inv.getProject().getName();
            } else if (data instanceof TaskCommentResponse) {
                TaskCommentResponse c = (TaskCommentResponse) data;
                ids.projectId = c.getProjectId();
                ids.entityName = ENTITY_NAME_COMMENT;
            } else if (data instanceof TaskAttachmentResponse) {
                TaskAttachmentResponse a = (TaskAttachmentResponse) data;
                ids.projectId = a.getProjectId();
                ids.entityName = a.getFileName();
            } else if (data instanceof SubTaskResponse) {
                SubTaskResponse s = (SubTaskResponse) data;
                ids.projectId = s.getProjectId();
                ids.entityName = s.getTitle();
                ids.entityCode = s.getId() != null ? PREFIX_SUBTASK + s.getId() : null;
            } else if (data instanceof EpicResponse) {
                EpicResponse e = (EpicResponse) data;
                ids.projectId = e.getProjectId();
                ids.entityName = e.getName();
                ids.entityCode = e.getEpicCode();
            } else if (data instanceof TagResponse) {
                TagResponse t = (TagResponse) data;
                ids.projectId = t.getProjectId();
                ids.entityName = t.getName();
            } else if (data instanceof CompanyDetailsResponse) {
                CompanyDetailsResponse c = (CompanyDetailsResponse) data;
                ids.companyId = c.getCompanyId();
                ids.entityName = c.getCompanyName();
                ids.entityCode = c.getCompanyCode();
            } else if (data instanceof SprintResponse) {
                SprintResponse s = (SprintResponse) data;
                ids.projectId = s.getProjectId();
                ids.entityName = s.getName();
            }
        } catch (Exception e) {
            log.warn(LOG_ERROR_EXTRACT_ID, e.getMessage());
        }
        return ids;
    }

    private Integer extractId(Object result) {
        Object data = unwrapResult(result);
        if (data == null) return null;

        try {
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

    private Object unwrapResult(Object result) {
        if (result == null) return null;
        Object data = result;
        if (result instanceof ResponseEntity) {
            data = ((ResponseEntity<?>) result).getBody();
        }
        if (data instanceof ApiResponse) {
            data = ((ApiResponse<?>) data).getData();
        }
        return data;
    }
     
    private String formatActionHeader(LogActivity logActivity) {
        String action = logActivity.action().toLowerCase().replace("_", " ");
        String type = logActivity.entityType().toLowerCase().replace("_", " ");
        return action + " a " + type;
    }

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

    // --- LOP DTO HO TRO ---
    
    private static class ContextIds {
        Integer companyId;
        Integer workspaceId;
        Integer projectId;
        String entityName;
        String entityCode;
        String targetName;
    }
}