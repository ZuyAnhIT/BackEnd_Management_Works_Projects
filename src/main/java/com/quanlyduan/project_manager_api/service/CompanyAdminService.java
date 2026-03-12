package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.company.AdminCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.company.Tenant360Response;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;

public interface CompanyAdminService {
    // API 1: Hiển thị danh sách phân trang (Không lọc)
    PageResponseDTO<AdminCompanyResponse> getCompanies(int page, int size, String sortBy, String sortDir);

    // API 2: Tìm kiếm nâng cao phân trang
    PageResponseDTO<AdminCompanyResponse> searchCompanies(
            String searchName, String searchCode, String searchEmail, 
            String searchStatus, String searchPlanCode, 
            int page, int size, String sortBy, String sortDir);
    
    // Lấy chi tiết thông số tài nguyên của 1 công ty
    Tenant360Response getTenant360View(Integer companyId);
}