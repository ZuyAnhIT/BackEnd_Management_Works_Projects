// File: src/main/java/com/quanlyduan/project_manager_api/service/SubTaskService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;

import java.util.List;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến Công việc phụ (SubTask).
 * Các thao tác luôn yêu cầu tham chiếu đến Project và Task cha (taskId) để đảm bảo tính toàn vẹn dữ liệu.
 */
public interface SubTaskService {
    
    // ========================================================================
    // 1. NHÓM XEM & LẤY (READ OPERATIONS)
    // ========================================================================
    
    /**
     * Lấy danh sách tất cả SubTask thuộc về một Task cha.
     * @param companyId ID Công ty
     * @param workspaceId ID Workspace
     * @param projectId ID Dự án
     * @param taskId ID Task cha
     * @return Danh sách SubTaskResponse DTO
     */
    List<SubTaskResponse> getSubTasks(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId);
    
    /**
     * Lấy thông tin chi tiết của một SubTask cụ thể.
     * @param subTaskId ID SubTask cần xem
     * @return SubTaskResponse DTO
     */
    SubTaskResponse getSubTaskDetail(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId);

    // ========================================================================
    // 2. NHÓM TẠO & CẬP NHẬT (CRUD OPERATIONS)
    // ========================================================================

    /**
     * Tạo một SubTask mới và gán vào Task cha (taskId).
     * @param taskId ID Task cha
     * @param request DTO chứa tiêu đề, mô tả, assignee...
     * @return SubTaskResponse DTO của SubTask vừa tạo
     */
    SubTaskResponse createSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, CreateSubTaskRequest request);
    
    /**
     * Cập nhật thông tin chi tiết của một SubTask.
     * @param subTaskId ID SubTask cần cập nhật
     * @param request DTO chứa các trường cập nhật (Partial Update)
     * @return SubTaskResponse DTO đã cập nhật
     */
    SubTaskResponse updateSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId, UpdateSubTaskRequest request);
    
    /**
     * Xóa một SubTask khỏi hệ thống.
     * @param subTaskId ID SubTask cần xóa
     */
    void deleteSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId);
}