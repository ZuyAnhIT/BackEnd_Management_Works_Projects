package com.quanlyduan.project_manager_api.aop;

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
            Integer userId = securityService.getCurrentUserId();
            if (userId == null) userId = 0;

            String ipAddress = "Unknown";
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                if (request != null) ipAddress = request.getRemoteAddr();
            } catch (Exception ignored) {}

            Integer entityId = extractIdFromResult(result);

            String messageDetail = "User " + userId + " performed " + logActivity.action() + " on " + logActivity.entityType();
            if (entityId != null) messageDetail += " (ID: " + entityId + ")";

            ActivityLog logEntity = ActivityLog.builder()
                    .userId(userId)
                    .action(logActivity.action())
                    .entityType(logActivity.entityType())
                    .entityId(entityId)
                    .newValue(messageDetail)
                    .ipAddress(ipAddress)
                    .build();

            activityLogService.saveLog(logEntity);
        } catch (Exception e) {
            log.error("Error saving activity log: {}", e.getMessage());
        }
    }

    private Integer extractIdFromResult(Object result) {
        if (result == null) return null;
        // Xử lý ApiResponse
        if (result instanceof com.quanlyduan.project_manager_api.dto.response.ApiResponse) {
            result = ((com.quanlyduan.project_manager_api.dto.response.ApiResponse<?>) result).getData();
        }
        if (result == null) return null;

        try {
            Method getIdMethod = result.getClass().getMethod("getId");
            Object idObj = getIdMethod.invoke(result);
            if (idObj instanceof Integer) return (Integer) idObj;
        } catch (Exception e) {
            // Thử getTaskId nếu getId không có
            try {
                Method getTaskId = result.getClass().getMethod("getTaskId");
                Object idObj = getTaskId.invoke(result);
                return (Integer) idObj;
            } catch (Exception ex) {}
        }
        return null;
    }
}
