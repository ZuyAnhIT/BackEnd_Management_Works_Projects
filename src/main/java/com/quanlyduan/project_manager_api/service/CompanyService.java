package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.model.CongTy;

public interface CompanyService {
    CongTy createCompany(CreateCompanyRequest request);
}