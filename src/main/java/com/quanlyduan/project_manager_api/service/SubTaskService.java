package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.*;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;
import java.util.List;

public interface SubTaskService {

    // --- Các hàm Ghi (Create/Update) - Đã có ---
    SubTaskResponse createSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, CreateSubTaskRequest request);

    SubTaskResponse updateSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId, UpdateSubTaskRequest request);

    // --- [CẦN THÊM MỚI] Các hàm Đọc (Read) để sửa lỗi ---

    /**
     * Lấy chi tiết SubTask
     */
    SubTaskResponse getSubTaskById(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId);

    /**
     * Lấy danh sách SubTask của một Task cha
     */
    List<SubTaskResponse> getAllSubTasksByTaskId(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId);
}