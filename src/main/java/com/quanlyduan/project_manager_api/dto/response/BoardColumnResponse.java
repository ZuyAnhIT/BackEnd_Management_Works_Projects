package com.quanlyduan.project_manager_api.dto.response;


import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BoardColumnResponse {
    private Integer statusId;
    private String statusName;
    private String statusColor;
    private Integer sortOrder;
    private Boolean isCompletedStatus;
    private List<TaskSummaryResponse> tasks;
}