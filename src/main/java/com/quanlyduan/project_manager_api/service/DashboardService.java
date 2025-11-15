package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;

public interface DashboardService {

    List<MyCompanyResponse> getMyCompanies();
}
