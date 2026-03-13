package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.company.AdminCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.company.Tenant360Response;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus; 
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus; 
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository; 
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository; 
import com.quanlyduan.project_manager_api.repository.specification.CompanySpecification;
import com.quanlyduan.project_manager_api.service.CompanyAdminService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    
    private final CompanyMemberRepository companyMemberRepository;
    private final ProjectRepository projectRepository;

    // =================================================================================
    // 1. HIỂN THỊ DANH SÁCH (GET ALL)
    // =================================================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<AdminCompanyResponse> getCompanies(int page, int size, String sortBy, String sortDir) {
        
        Map<String, String> sortMapping = Map.of(
                "createdAt", "createdAt",
                "name", "name",
                "code", "companyCode",
                "email", "email",
                "status", "status",
                "storage", "currentStorageBytes"
        );

        String actualSortField = sortMapping.getOrDefault(sortBy, "createdAt");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(actualSortField).ascending() : Sort.by(actualSortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Company> companiesPage = companyRepository.findAll(pageable);

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

        Map<String, String> sortMapping = Map.of(
                "createdAt", "createdAt",
                "name", "name",
                "code", "companyCode",
                "email", "email",
                "status", "status",
                "storage", "currentStorageBytes"
        );

        String actualSortField = sortMapping.getOrDefault(sortBy, "createdAt");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(actualSortField).ascending() : Sort.by(actualSortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Company> spec = CompanySpecification.filterCompaniesForAdmin(
                searchName, searchCode, searchEmail, searchStatus, searchPlanCode);

        Page<Company> companiesPage = companyRepository.findAll(spec, pageable);

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

    // =================================================================================
    // 3. TENANT 360-DEGREE VIEW (XEM CHI TIẾT)
    // =================================================================================
    @Override
    @Transactional(readOnly = true)
    public Tenant360Response getTenant360View(Integer companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        CompanySubscription sub = company.getSubscription();
        String planCode = "N/A", planName = "No Plan", subStatus = "NONE";
        Integer maxUsers = 0, maxProjects = 0;
        long maxStorageBytes = 0;
        BigDecimal price = BigDecimal.ZERO; 
        LocalDateTime start = null, end = null;

        if (sub != null && sub.getPlan() != null) {
            planCode = sub.getPlan().getPlanCode();
            planName = sub.getPlan().getName();
            // 👉 Đã sửa biến monthlyPrice thành price để gán đúng
            price = sub.getPlan().getMonthlyPrice();
            subStatus = sub.getStatus().toString();
            start = sub.getCurrentPeriodStart();
            end = sub.getCurrentPeriodEnd();
            
            maxUsers = sub.getPlan().getMaxUsers();
            maxProjects = sub.getPlan().getMaxProjects();
            
            if (sub.getPlan().getMaxStorageGb() != -1) {
                maxStorageBytes = sub.getPlan().getMaxStorageGb() * 1073741824L;
            } else {
                maxStorageBytes = -1; 
            }
        }

        long currentMembers = companyMemberRepository.countByCompany_IdAndStatusNot(companyId, MemberStatus.REMOVED);
        long currentProjects = projectRepository.countByWorkspace_Company_IdAndStatusNot(companyId, ProjectStatus.CANCELLED); 

        return Tenant360Response.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .email(company.getEmail())
                .status(company.getStatus().toString())
                .createdAt(company.getCreatedAt())
                
                .planCode(planCode)
                .planName(planName)
                .monthlyPrice(price)
                .subscriptionStatus(subStatus)
                .currentPeriodStart(start)
                .currentPeriodEnd(end)
                
                .totalMembers(currentMembers)
                .maxUsers(maxUsers)
                
                .totalProjects(currentProjects)
                .maxProjects(maxProjects)
                
                .currentStorageBytes(company.getCurrentStorageBytes() != null ? company.getCurrentStorageBytes() : 0)
                .maxStorageBytes(maxStorageBytes)
                .build();
    }


    @Override
    @Transactional
    public void changeCompanyStatus(Integer companyId, String newStatus) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        try {
            CompanyStatus statusEnum = CompanyStatus.valueOf(newStatus.toUpperCase());
            company.setStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            // Bắt lỗi nếu lỡ truyền vào một status tào lao không có trong Enum
            throw new BadRequestException("Invalid status value: " + newStatus);
        }
        
        companyRepository.save(company);
    }
}