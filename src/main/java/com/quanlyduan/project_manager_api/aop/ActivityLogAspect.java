package com.quanlyduan.project_manager_api.aop;

import com.quanlyduan.project_manager_api.dto.response.*; // Import * cho gọn
import com.quanlyduan.project_manager_api.model.ActivityLog;
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

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLogAspect {

    private final ActivityLogService activityLogService;
    private final SecurityService securityService;

    @AfterReturning(pointcut = "@annotation(logActivity)", returning = "result")
    public void logAfterMethod(JoinPoint joinPoint, LogActivity logActivity, Object result) {
        try {
            Integer userId = 0;
            try {
                userId = securityService.getCurrentUserId();
            } catch (Exception e) {}

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
            String description = generateJiraStyleDescription(logActivity, result, contextIds);

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

        } catch (Exception e) {
            log.error("Error saving activity log: {}", e.getMessage());
        }
    }

    private static class ContextIds {
        Integer companyId;
        Integer workspaceId;
        Integer projectId;
        String entityName;
        String entityCode;
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
                ids.workspaceId = t.getWorkspaceId();
                ids.companyId = t.getCompanyId();
                ids.entityName = t.getTitle();
                ids.entityCode = t.getTaskCode();
            }
            // 2. TASK DETAIL
            else if (data instanceof TaskResponse) {
                TaskResponse t = (TaskResponse) data;
                if (t.getProject() != null) ids.projectId = t.getProject().getId();
                ids.workspaceId = t.getWorkspaceId();
                ids.companyId = t.getCompanyId();
                ids.entityName = t.getTitle();
                ids.entityCode = t.getTaskCode();
            }
            // 3. PROJECT
            else if (data instanceof ProjectResponse) {
                ProjectResponse p = (ProjectResponse) data;
                ids.projectId = p.getId();
                ids.workspaceId = p.getWorkspaceId();
                ids.companyId = p.getCompanyId();
                ids.entityName = p.getName();
                ids.entityCode = p.getProjectCode();
            }
            // 4. WORKSPACE (SỬA LỖI LOGIC CŨ Ở ĐÂY)
            else if (data instanceof WorkspaceResponse) {
                WorkspaceResponse w = (WorkspaceResponse) data;
                ids.workspaceId = w.getWorkspaceId();
                ids.companyId = w.getCompanyId();
                ids.entityName = w.getWorkspaceName();
                // ids.entityCode = w.getWorkspaceCode(); 
            }
            // 5. COMMENT
            else if (data instanceof TaskCommentResponse) {
                TaskCommentResponse c = (TaskCommentResponse) data;
                ids.projectId = c.getProjectId();
                ids.workspaceId = c.getWorkspaceId();
                ids.companyId = c.getCompanyId();
                ids.entityName = "Comment on Task";
            }
            // 6. ATTACHMENT
            else if (data instanceof TaskAttachmentResponse) {
                TaskAttachmentResponse a = (TaskAttachmentResponse) data;
                ids.projectId = a.getProjectId();
                ids.workspaceId = a.getWorkspaceId();
                ids.companyId = a.getCompanyId();
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
    private ContextIds extractContextIdsFromRequest() {
    ContextIds ids = new ContextIds();
    try {
        HttpServletRequest req = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        String[] parts = req.getRequestURI().split("/");

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("companies")) {
                ids.companyId = Integer.valueOf(parts[i + 1]);
            }
            if (parts[i].equals("workspaces")) {
                ids.workspaceId = Integer.valueOf(parts[i + 1]);
            }
            if (parts[i].equals("projects")) {
                ids.projectId = Integer.valueOf(parts[i + 1]);
            }
        }
    } catch (Exception ignored) {}
    return ids;
}

    private Integer extractId(Object result) {
        if (result == null) return null;
        Object data = result;
        if (result instanceof org.springframework.http.ResponseEntity) {
            data = ((org.springframework.http.ResponseEntity<?>) result).getBody();
        }
        if (data instanceof com.quanlyduan.project_manager_api.dto.response.ApiResponse) {
            data = ((com.quanlyduan.project_manager_api.dto.response.ApiResponse<?>) data).getData();
        }
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
        // 1. Thử tìm getTaskId
        try {
            Method getTaskId = data.getClass().getMethod("getTaskId");
            return (Integer) getTaskId.invoke(data);
        } catch (Exception e) {}

        // 2. Thử tìm getProjectId
        try {
            Method getProjectId = data.getClass().getMethod("getProjectId");
            return (Integer) getProjectId.invoke(data);
        } catch (Exception e) {}

        // 3. Thử tìm getWorkspaceId (BỔ SUNG)
        try {
            Method getWorkspaceId = data.getClass().getMethod("getWorkspaceId");
            return (Integer) getWorkspaceId.invoke(data);
        } catch (Exception e) {}

        // 4. Thử tìm getCompanyId (BỔ SUNG)
        try {
            Method getCompanyId = data.getClass().getMethod("getCompanyId");
            return (Integer) getCompanyId.invoke(data);
        } catch (Exception e) {}
        
        // 5. Thử tìm getCommentId (BỔ SUNG - Cho Comment)
        try {
            Method getCommentId = data.getClass().getMethod("getCommentId");
            return (Integer) getCommentId.invoke(data);
        } catch (Exception e) {}

        return null; // Chịu thua
    }

    private String generateJiraStyleDescription(LogActivity logActivity, Object result, ContextIds context) {
        String action = logActivity.action().toLowerCase();
        String type = logActivity.entityType().toLowerCase();

        StringBuilder sb = new StringBuilder();
        sb.append(action).append(" a ").append(type);

        if (context.entityCode != null) {
            sb.append(" <strong>").append(context.entityCode).append("</strong>");
        } else if (context.entityName != null) {
            sb.append(" \"").append(context.entityName).append("\"");
        }
        return sb.toString();
    }
}