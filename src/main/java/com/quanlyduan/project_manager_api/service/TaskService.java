// File: src/main/java/com/quanlyduan/project_manager_api/service/TaskService.java
// (MỚI)
package com.quanlyduan.project_manager_api.service;

// import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.model.Task; // Cần cho hàm map

public interface TaskService {
    // (Helper để các service khác tái sử dụng)
    TaskResponse mapToTaskResponse(Task task);
}
