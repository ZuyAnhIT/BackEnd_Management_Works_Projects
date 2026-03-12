package com.quanlyduan.project_manager_api.controller.admin;

import com.quanlyduan.project_manager_api.dto.response.company.AdminCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.service.CompanyAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
public class AdminCompanyController {

    private final CompanyAdminService companyAdminService;

    // 1. API LẤY DANH SÁCH (GET ALL)
    @GetMapping
    @PreAuthorize("@securityService.hasSystemPermission('tenant:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<AdminCompanyResponse>>> getCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponseDTO<AdminCompanyResponse> response = companyAdminService.getCompanies(page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success("Fetched companies successfully.", response));
    }

    // 2. API TÌM KIẾM (SEARCH)
    @GetMapping("/search")
    @PreAuthorize("@securityService.hasSystemPermission('tenant:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<AdminCompanyResponse>>> searchCompanies(
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) String searchCode,
            @RequestParam(required = false) String searchEmail,
            @RequestParam(required = false) String searchStatus,
            @RequestParam(required = false) String searchPlanCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponseDTO<AdminCompanyResponse> response = companyAdminService.searchCompanies(
                searchName, searchCode, searchEmail, searchStatus, searchPlanCode, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success("Search companies successfully.", response));
    }
}