// File: src/main/java/com/quanlyduan/project_manager_api/service/SubTaskService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;

public interface SubTaskService {
    
    /**
     * Tạo SubTask mới cho một Task
     */
    SubTaskResponse createSubTask(Integer taskId, CreateSubTaskRequest request);
}