// File: src/main/java/com/quanlyduan/project_manager_api/service/SprintService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.SprintDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.SprintResponse;
import java.util.List;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến Sprint.
 * Bao gồm quản lý vòng đời Sprint và các API hỗ trợ Security.
 */
public interface SprintService {
    
    // ========================================================================
    // 1. QUẢN LÝ VÒNG ĐỜI SPRINT (LIFECYCLE)
    // ========================================================================

    /**
     * Tạo một Sprint mới (Hỗ trợ Quick Create).
     * @param projectId ID dự án chứa Sprint.
     * @param request DTO chứa thông tin Sprint.
     */
    SprintResponse createSprint(Integer projectId, CreateSprintRequest request); 

    /**
     * Cập nhật thông tin chi tiết Sprint (Tên, Mục tiêu, Ngày tháng).
     * @param projectId ID dự án (kiểm tra IDOR).
     * @param sprintId ID Sprint cần cập nhật.
     * @param request DTO chứa các trường cập nhật (Partial Update).
     */
    SprintResponse updateSprint(Integer projectId, Integer sprintId, UpdateSprintRequest request);

    /**
     * Bắt đầu một Sprint (Chuyển trạng thái sang IN_PROGRESS).
     * @param projectId ID dự án.
     * @param sprintId ID Sprint cần bắt đầu.
     */
    SprintResponse startSprint(Integer projectId, Integer sprintId);
    
    /**
     * Hoàn thành một Sprint (Chuyển trạng thái sang COMPLETED).
     * Logic nghiệp vụ: Tự động đẩy task chưa hoàn thành về Backlog.
     * @param projectId ID dự án.
     * @param sprintId ID Sprint cần hoàn thành.
     */
    SprintResponse completeSprint(Integer projectId, Integer sprintId);

    /**
     * Xử lý xóa Sprint thông minh (Smart Delete).
     * Logic nghiệp vụ: Hard Delete nếu Sprint rỗng và chưa bắt đầu, ngược lại là Soft Delete (CANCELLED).
     * @param projectId ID dự án.
     * @param sprintId ID Sprint cần xóa/hủy.
     */
    void deleteSprint(Integer projectId, Integer sprintId);

    // ========================================================================
    // 2. XEM & LẤY DỮ LIỆU (READ)
    // ========================================================================

    /**
     * Lấy danh sách Sprint của Dự án.
     * @param projectId ID dự án.
     * @param status Trạng thái lọc (ví dụ: "COMPLETED").
     * @return Danh sách DTO.
     */
    List<SprintResponse> getSprintsByProject(Integer projectId, String status); 

    /**
     * Lấy thông tin chi tiết của một Sprint.
     * Bao gồm danh sách task bên trong và các chỉ số thống kê (Story Points, Task Count).
     * @param projectId ID của Project (để kiểm tra bảo mật).
     * @param sprintId ID của Sprint.
     * @return Chi tiết Sprint (DTO).
     */
    SprintDetailsResponse getSprintDetails(Integer projectId, Integer sprintId);

    // ========================================================================
    // 3. HỖ TRỢ SECURITY (SECURITY HELPERS)
    // ========================================================================

    /**
     * Lấy Project ID dựa trên Sprint ID.
     * Hàm này được sử dụng bởi SecurityService để kiểm tra quyền hạn thừa kế.
     * @param sprintId ID Sprint.
     * @return Project ID cha.
     */
    Integer getProjectIdBySprint(Integer sprintId);
}