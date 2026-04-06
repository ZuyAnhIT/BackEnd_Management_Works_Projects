package com.quanlyduan.project_manager_api.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonalDashboardResponse {
    
    private List<MyTaskResponse> overdue;
    
    private List<MyTaskResponse> today;
    
    private List<MyTaskResponse> upcoming;
    
    private List<MyTaskResponse> noDueDate;
    
    private List<MyTaskResponse> other;
}