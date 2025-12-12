// File: src/main/java/com/quanlyduan/project_manager_api/service/TaskService.java
package com.quanlyduan.project_manager_api.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.ImportResultResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskImportPreviewResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.model.Task;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến Công việc (Task).
 * Bao gồm CRUD, quản lý vòng đời (Sprint, Status) và các thao tác kéo thả.
 */
public interface TaskService {

    // ========================================================================
    // 1. NHÓM TẠO/CẬP NHẬT/XEM (CRUD)
    // ========================================================================

    /**
     * Tạo một Task mới trong dự án (Hỗ trợ Quick Create).
     * @param projectId ID dự án mà task thuộc về.
     * @param request DTO chứa thông tin task (chỉ title là bắt buộc).
     * @return TaskSummaryResponse DTO của task vừa tạo.
     */
    TaskSummaryResponse createTask(Integer projectId, CreateTaskRequest request);

    /**
     * Lấy chi tiết đầy đủ của một Task.
     * @param taskId ID Task cần xem.
     * @return TaskResponse DTO chi tiết.
     */
    TaskResponse getTaskDetails(Integer taskId);

    /**
     * Cập nhật thông tin Task (Sửa tiêu đề, mô tả, hạn chót, metrics...).
     * @param taskId ID Task cần cập nhật.
     * @param request DTO chứa các trường cập nhật (Partial Update).
     * @return TaskResponse sau khi cập nhật.
     */
    TaskResponse updateTask(Integer taskId, UpdateTaskRequest request);
    
    // ========================================================================
    // 2. NHÓM THAO TÁC KÉO THẢ (DRAG & DROP ACTIONS)
    // ========================================================================

    /**
     * Cập nhật Sprint cho một Task (Kéo thả Task vào/ra khỏi Sprint/Backlog).
     * @param taskId ID của task.
     * @param sprintId ID của Sprint mới (hoặc null nếu về Backlog).
     * @param newSortOrder Vị trí sắp xếp mới của Task trong danh sách đích.
     */
    TaskResponse updateTaskSprint(Integer taskId, Integer sprintId, Integer newSortOrder);
    
    /**
     * Di chuyển Task sang một trạng thái (cột) khác trên Board.
     * @param taskId ID của task cần di chuyển.
     * @param request DTO chứa ID trạng thái mới và vị trí sắp xếp mới (newSortOrder).
     */
    TaskResponse moveTaskToStatus(Integer taskId, MoveTaskStatusRequest request);
    
    /**
     * Gán hoặc gỡ Epic khỏi Task.
     * @param taskId ID của Task cần cập nhật.
     * @param request Chứa epicId (hoặc null để gỡ).
     * @return TaskResponse sau khi cập nhật.
     */
    TaskResponse updateTaskEpic(Integer taskId, UpdateTaskEpicRequest request);
    TaskResponse deleteTask(Integer taskId);

    // Lưu trữ task
    void archiveTask(Integer taskId);

    // Khôi phục task
    void restoreTask(Integer taskId);

    // ========================================================================
    // 3. HÀM HELPER
    // ========================================================================

    /**
     * Hàm helper để map Task (Entity) sang TaskResponse (DTO chi tiết).
     * @param task Entity Task.
     * @return TaskResponse DTO.
     */
    TaskResponse mapToTaskResponse(Task task);
    List<TaskImportPreviewResponse> previewImportTasks(Integer projectId, MultipartFile file);
    ImportResultResponse saveImportedTasks(Integer projectId, List<TaskImportPreviewResponse> validatedRows);
    ImportResultResponse importTasksFromCsv(Integer projectId, MultipartFile file);
}