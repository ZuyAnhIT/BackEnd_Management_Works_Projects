// File: src/main/java/com/quanlyduan/project_manager_api/service/ProjectStatusService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.ReorderStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectStatusResponse;
import java.util.List;

public interface ProjectStatusService {
    
    /**
     * Lấy danh sách các trạng thái (cột) của một dự án.
     * @param projectId ID dự án
     * @return Danh sách DTO đã sắp xếp
     */
    List<ProjectStatusResponse> getProjectStatuses(Integer projectId);

    /**
     * Tạo một trạng thái (cột) mới cho dự án.
     * @param projectId ID dự án
     * @param request DTO tạo mới
     * @return DTO status vừa tạo
     */
    ProjectStatusResponse createStatus(Integer projectId, CreateProjectStatusRequest request);

    // *** THÊM PHƯƠNG THỨC NÀY ***
    /**
     * Sắp xếp lại thứ tự các trạng thái (cột) trong dự án.
     * @param projectId ID dự án
     * @param request DTO chứa danh sách ID theo thứ tự mới
     */
    void reorderStatuses(Integer projectId, ReorderStatusRequest request);
    
    /**
     * Xóa một trạng thái (cột).
     * Chỉ xóa được nếu cột rỗng (không có task).
     * @param projectId ID dự án (để kiểm tra bảo mật)
     * @param statusId ID trạng thái cần xóa
     */
    void deleteStatus(Integer projectId, Integer statusId);

    /**
     * Cập nhật thông tin trạng thái (tên, màu sắc, cờ hoàn thành).
     */
    ProjectStatusResponse updateStatus(Integer projectId, Integer statusId, UpdateStatusRequest request);
   
    // (Các hàm create, reorder, delete sẽ làm ở bước sau)
}