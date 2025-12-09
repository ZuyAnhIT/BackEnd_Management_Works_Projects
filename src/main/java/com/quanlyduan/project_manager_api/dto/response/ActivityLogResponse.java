package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivityLogResponse {
    private Integer id;
    private String userName;
    private String userAvatar;
    private String action;
    private String entityType;
    private Integer entityId;
    private String description;
    private LocalDateTime timestamp;
    private String timeAgo; 
}
