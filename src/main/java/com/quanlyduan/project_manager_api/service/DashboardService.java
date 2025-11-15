package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import java.util.List;

public interface DashboardService {

    // (Có thể đã có các phương thức khác ở đây)

    /**
     * US-S3-3: Lấy danh sách các project mà người dùng hiện tại đang tham gia.
     * @return Danh sách MyProjectResponse
     */
    List<MyProjectResponse> getMyProjects();
}