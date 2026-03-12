package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.company.AdminCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.service.CompanyAdminService;
import com.quanlyduan.project_manager_api.repository.specification.CompanySpecification;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyAdminServiceImpl implements CompanyAdminService {

    private final CompanyRepository companyRepository;

    // =================================================================================
    // 1. HIỂN THỊ DANH SÁCH (GET ALL)
    // =================================================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<AdminCompanyResponse> getCompanies(int page, int size, String sortBy, String sortDir) {
        
        // 1. Cấu hình Map ánh xạ cho việc sắp xếp
        Map<String, String> sortMapping = Map.of(
                "createdAt", "createdAt",
                "name", "name",
                "code", "companyCode",
                "email", "email",
                "status", "status",
                "storage", "currentStorageBytes"
        );

        // 2. Lấy field thật để sort (Nếu truyền sai thì mặc định là createdAt)
        String actualSortField = sortMapping.getOrDefault(sortBy, "createdAt");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(actualSortField).ascending() : Sort.by(actualSortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. Gọi Repository
        Page<Company> companiesPage = companyRepository.findAll(pageable);

        // 4. Map sang DTO và trả về
        Page<AdminCompanyResponse> dtoPage = companiesPage.map(this::mapToAdminResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // =================================================================================
    // 2. TÌM KIẾM NÂNG CAO (SEARCH)
    // =================================================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<AdminCompanyResponse> searchCompanies(
            String searchName, String searchCode, String searchEmail, 
            String searchStatus, String searchPlanCode, 
            int page, int size, String sortBy, String sortDir) {

        // 1. Cấu hình Map ánh xạ
        Map<String, String> sortMapping = Map.of(
                "createdAt", "createdAt",
                "name", "name",
                "code", "companyCode",
                "email", "email",
                "status", "status",
                "storage", "currentStorageBytes"
        );

        // 2. Tạo Pageable
        String actualSortField = sortMapping.getOrDefault(sortBy, "createdAt");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(actualSortField).ascending() : Sort.by(actualSortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. Tạo Specification (Bộ lọc động)
        Specification<Company> spec = CompanySpecification.filterCompaniesForAdmin(
                searchName, searchCode, searchEmail, searchStatus, searchPlanCode);

        // 4. Gọi Repository với Specification
        Page<Company> companiesPage = companyRepository.findAll(spec, pageable);

        // 5. Map sang DTO và trả về
        Page<AdminCompanyResponse> dtoPage = companiesPage.map(this::mapToAdminResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // =================================================================================
    // HÀM HELPER: MAP ENTITY SANG DTO
    // =================================================================================
    private AdminCompanyResponse mapToAdminResponse(Company company) {
        AdminCompanyResponse.AdminCompanyResponseBuilder builder = AdminCompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .companyCode(company.getCompanyCode())
                .email(company.getEmail())
                .phoneNumber(company.getPhoneNumber())
                .currentStorageBytes(company.getCurrentStorageBytes())
                .isVerifiedTenant(company.getIsVerifiedTenant())
                .status(company.getStatus().toString())
                .createdAt(company.getCreatedAt());

        CompanySubscription sub = company.getSubscription(); 
        if (sub != null) {
            builder.subscriptionStatus(sub.getStatus().toString())
                   .currentPeriodEnd(sub.getCurrentPeriodEnd());
            
            if (sub.getPlan() != null) {
                builder.planCode(sub.getPlan().getPlanCode())
                       .planName(sub.getPlan().getName());
            }
        }

        return builder.build();
    }
}