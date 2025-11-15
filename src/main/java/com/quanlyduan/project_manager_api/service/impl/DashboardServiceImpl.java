package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final SecurityService securityService;
    private final CompanyMemberRepository companyMemberRepository;

    public DashboardServiceImpl(SecurityService securityService,
                                CompanyMemberRepository companyMemberRepository) {
        this.securityService = securityService;
        this.companyMemberRepository = companyMemberRepository;
    }

    @Override
    public List<MyCompanyResponse> getMyCompanies() {
        Integer userId = securityService.getCurrentUserId();

        return companyMemberRepository.findByUser_Id(userId)
                .stream()
                .map(this::mapToMyCompanyResponse)
                .collect(Collectors.toList());
    }

    private MyCompanyResponse mapToMyCompanyResponse(CompanyMember companyMember) {
        Company company = companyMember.getCompany();

        return MyCompanyResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .companyCode(company.getCompanyCode())
                .description(company.getDescription())
                .logoUrl(company.getLogoUrl())
                .roleCode(companyMember.getRole().getRoleCode())
                .memberStatus(companyMember.getStatus().name())
                .jobTitle(companyMember.getJobTitle())
                .department(companyMember.getDepartment())
                .joinedAt(companyMember.getJoinedAt())
                .build();
    }
}
