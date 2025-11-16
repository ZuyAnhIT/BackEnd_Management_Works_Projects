package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;

public interface DashboardService {

    List<MyCompanyResponse> getMyCompanies();

    // (Có thể đã có các phương thức khác ở đây)

    /**
     * US-S3-3: Lấy danh sách các project mà người dùng hiện tại đang tham gia.
     * @return Danh sách MyProjectResponse
     */
    List<MyProjectResponse> getMyProjects();
}
