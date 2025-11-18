// File: src/main/java/com/quanlyduan/project_manager_api/service/TaskService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.model.Task;

public interface TaskService {

    /**
     * Tạo một Task mới trong dự án.
     * @param projectId ID dự án mà task thuộc về
     * @param request DTO chứa thông tin task
     * @return TaskSummaryResponse DTO của task vừa tạo
     */
    TaskSummaryResponse createTask(Integer projectId, CreateTaskRequest request);
    
    /**
     * Cập nhật Sprint cho một Task (kéo/thả vào Backlog hoặc Sprint).
     * @param taskId ID của task
     * @param sprintId ID của Sprint mới (hoặc null nếu về Backlog)
     */
    void updateTaskSprint(Integer taskId, Integer sprintId);
    
    /**
     * Hàm helper để map Task (Entity) sang TaskResponse (DTO chi tiết).
     * (Hàm này có thể được chuyển sang private hoặc một Mapper riêng sau này)
     */
    TaskResponse mapToTaskResponse(Task task);

    /**
     * Di chuyển Task sang một trạng thái (cột) khác.
     * @param taskId ID của task cần di chuyển
     * @param request DTO chứa ID trạng thái mới
     */
    void moveTaskToStatus(Integer taskId, MoveTaskStatusRequest request);
}