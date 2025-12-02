// File: src/main/java/com.quanlyduan.project_manager_api/service/EpicService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateEpicRequest;
import com.quanlyduan.project_manager_api.dto.response.EpicResponse;
import java.util.List;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến Epic (Mục tiêu lớn/Sử thi).
 * Epic là container chứa các Task con và Story trong quá trình lập kế hoạch và quản lý dự án.
 */
public interface EpicService {

    // ========================================================================
    // 1. XEM & LỌC (READ & FILTER)
    // ========================================================================

    /**
     * Lấy danh sách tất cả các Epic thuộc về một dự án.
     * Hỗ trợ tìm kiếm theo từ khóa (keyword) trong tên Epic.
     * @param projectId ID dự án.
     * @param keyword Từ khóa tìm kiếm (tên Epic).
     * @return Danh sách DTO các Epic của dự án.
     */
    List<EpicResponse> getEpicsByProject(Integer projectId, String keyword);

    /**
     * Lấy thông tin chi tiết của một Epic.
     * @param projectId ID của dự án (để kiểm tra bảo mật/phạm vi)
     * @param epicId ID của Epic cần lấy
     * @return DTO thông tin Epic
     */
    EpicResponse getEpicDetails(Integer projectId, Integer epicId);

    // ========================================================================
    // 2. TẠO & CẬP NHẬT (CRUD)
    // ========================================================================
    
    /**
     * Tạo một Epic mới cho Dự án.
     * Logic nghiệp vụ: Tự động sinh mã Epic (Epic Code) và gán trạng thái mặc định (OPEN).
     * @param projectId ID dự án.
     * @param request DTO tạo mới.
     * @return DTO Epic vừa tạo.
     */
    EpicResponse createEpic(Integer projectId, CreateEpicRequest request);

    /**
     * Cập nhật thông tin Epic (Tên, Mô tả, Màu, Ngày).
     * @param projectId ID dự án (để kiểm tra IDOR).
     * @param epicId ID Epic cần cập nhật.
     * @param request DTO chứa các trường cập nhật (Partial Update).
     * @return DTO Epic sau khi cập nhật.
     */
    EpicResponse updateEpic(Integer projectId, Integer epicId, UpdateEpicRequest request);

    /**
     * Xóa một Epic khỏi Dự án.
     * Logic nghiệp vụ: Chặn xóa nếu Epic đang chứa Task (Restrict).
     * @param projectId ID dự án (để kiểm tra IDOR).
     * @param epicId ID Epic cần xóa.
     */
    void deleteEpic(Integer projectId, Integer epicId);
}