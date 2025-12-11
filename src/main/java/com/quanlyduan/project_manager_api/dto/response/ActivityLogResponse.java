package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityLogResponse {
    private Integer id;
    private String userName;
    private String userAvatar;
    private String action;
    private String entityType;
    private String entityName;
    private String entityCode;
    private Integer entityId;
    private String description;
    private LocalDateTime timestamp;
    private String timeAgo; 
}
