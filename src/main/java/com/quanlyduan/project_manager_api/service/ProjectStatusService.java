// File: src/main/java/com/quanlyduan/project_manager_api/service/ProjectStatusService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.ReorderStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectStatusResponse;
import java.util.List;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến Trạng thái (Cột) của Dự án.
 * Đây là các API cho phép quản lý cấu trúc Board (thêm, xóa, sắp xếp cột).
 */
public interface ProjectStatusService {
    
    // ========================================================================
    // 1. NHÓM XEM & LỌC (READ)
    // ========================================================================

    /**
     * Lấy danh sách tất cả các Trạng thái (cột) thuộc về một dự án.
     * Dữ liệu trả về đã được sắp xếp theo sortOrder để vẽ giao diện Board.
     * @param projectId ID dự án.
     * @return Danh sách DTO đã sắp xếp.
     */
    List<ProjectStatusResponse> getProjectStatuses(Integer projectId);

    // ========================================================================
    // 2. NHÓM TẠO & CẬP NHẬT & SẮP XẾP (MUTATION/CRUD)
    // ========================================================================

    /**
     * Tạo một Trạng thái (cột) mới cho dự án.
     * Logic nghiệp vụ: Tự động tính toán vị trí cuối cùng (sortOrder).
     * @param projectId ID dự án.
     * @param request DTO tạo mới.
     * @return DTO status vừa tạo.
     */
    ProjectStatusResponse createStatus(Integer projectId, CreateProjectStatusRequest request);

    /**
     * Cập nhật thông tin chi tiết trạng thái (Tên, Mã màu, Cờ Hoàn thành).
     * @param projectId ID dự án (kiểm tra IDOR).
     * @param statusId ID trạng thái cần sửa.
     * @param request DTO thông tin cập nhật (Partial Update).
     * @return DTO đã cập nhật.
     */
    ProjectStatusResponse updateStatus(Integer projectId, Integer statusId, UpdateStatusRequest request);
    
    /**
     * Sắp xếp lại thứ tự các trạng thái (cột) trong dự án.
     * Hàm này thực hiện logic cập nhật `sortOrder` cho nhiều trạng thái cùng lúc (Kéo thả cột).
     * @param projectId ID dự án.
     * @param request DTO chứa danh sách ID theo thứ tự mới.
     */
    void reorderStatuses(Integer projectId, ReorderStatusRequest request);
    
    /**
     * Xóa một Trạng thái (cột).
     * Logic nghiệp vụ: Chỉ xóa được nếu cột rỗng (không có task).
     * @param projectId ID dự án (kiểm tra bảo mật).
     * @param statusId ID trạng thái cần xóa.
     */
    void deleteStatus(Integer projectId, Integer statusId);
}