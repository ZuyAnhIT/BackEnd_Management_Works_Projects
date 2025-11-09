package com.quanlyduan.project_manager_api.repository.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ProjectView {
    Integer getId();
    Integer getWorkspaceId();
    Integer getProjectTypeId();
    String getName();
    String getProjectCode();
    String getDescription();
    String getCoverImageUrl();
    String getGoal();
    String getStatus();
    String getPriority();
    LocalDate getStartDate();
    LocalDate getDueDate();
    Integer getManagerId();
    Integer getCreatedById();
    String getBoardConfig();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}


