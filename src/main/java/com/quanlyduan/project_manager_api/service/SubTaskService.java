package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.*;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;
import java.util.List;

public interface SubTaskService {
    SubTaskResponse createSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, CreateSubTaskRequest request);
    SubTaskResponse updateSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId, UpdateSubTaskRequest request);
}
