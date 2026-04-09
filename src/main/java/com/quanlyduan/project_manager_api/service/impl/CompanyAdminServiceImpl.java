package com.quanlyduan.project_manager_api.service.impl;

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

import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.TransactionHistoryResponse;
import com.quanlyduan.project_manager_api.dto.response.company.AdminCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.company.Tenant360Response;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.TransactionRepository;
import com.quanlyduan.project_manager_api.repository.specification.CompanySpecification;
import com.quanlyduan.project_manager_api.service.CompanyAdminService;

@Service
public class CompanyAdminServiceImpl implements CompanyAdminService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_COMPANY_CODE = "companyCode";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_STORAGE = "storage";
    public static final String FIELD_CURRENT_STORAGE_BYTES = "currentStorageBytes";
    
    public static final String SORT_ASC = "asc";
    
    public static final String ERROR_COMPANY_NOT_FOUND = "Company not found.";
    public static final String ERROR_INVALID_STATUS = "Invalid status value: ";
    
    public static final String DEFAULT_PLAN_CODE = "N/A";
    public static final String DEFAULT_PLAN_NAME = "No Plan";
    public static final String DEFAULT_SUB_STATUS = "NONE";
    public static final String STATUS_PAST_DUE = "PAST_DUE";
    
    public static final long BYTES_IN_GB = 1073741824L;
    public static final int UNLIMITED_VALUE = -1;

    // Khai bao cac bien phu thuoc
    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final ProjectRepository projectRepository;
    private final TransactionRepository transactionRepository;

    // Constructor thay the cho annotation @RequiredArgsConstructor
    public CompanyAdminServiceImpl(CompanyRepository companyRepository,
                                   CompanyMemberRepository companyMemberRepository,
                                   ProjectRepository projectRepository,TransactionRepository transactionRepository) {
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.projectRepository = projectRepository;
        this.transactionRepository = transactionRepository;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---
    
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<AdminCompanyResponse> getCompanies(int page, int size, String sortBy, String sortDir) {
        // Anh xa truong sap xep de tranh loi truy van khi khong khop ten cot trong DB
        Map<String, String> sortMapping = Map.of(
                FIELD_CREATED_AT, FIELD_CREATED_AT,
                FIELD_NAME, FIELD_NAME,
                FIELD_CODE, FIELD_COMPANY_CODE,
                FIELD_EMAIL, FIELD_EMAIL,
                FIELD_STATUS, FIELD_STATUS,
                FIELD_STORAGE, FIELD_CURRENT_STORAGE_BYTES
        );

        // Xac dinh truong va huong sap xep
        String actualSortField = sortMapping.getOrDefault(sortBy, FIELD_CREATED_AT);
        Sort sort = sortDir.equalsIgnoreCase(SORT_ASC) ? Sort.by(actualSortField).ascending() : Sort.by(actualSortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Lay du lieu phan trang tu co so du lieu
        Page<Company> companiesPage = companyRepository.findAll(pageable);

        // Chuyen doi Entity sang DTO va tra ve ket qua
        Page<AdminCompanyResponse> dtoPage = companiesPage.map(this::mapToAdminResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<AdminCompanyResponse> searchCompanies(
            String searchName, String searchCode, String searchEmail, 
            String searchStatus, String searchPlanCode, 
            int page, int size, String sortBy, String sortDir) {

        // Anh xa truong sap xep tuong tu ham lay danh sach
        Map<String, String> sortMapping = Map.of(
                FIELD_CREATED_AT, FIELD_CREATED_AT,
                FIELD_NAME, FIELD_NAME,
                FIELD_CODE, FIELD_COMPANY_CODE,
                FIELD_EMAIL, FIELD_EMAIL,
                FIELD_STATUS, FIELD_STATUS,
                FIELD_STORAGE, FIELD_CURRENT_STORAGE_BYTES
        );

        String actualSortField = sortMapping.getOrDefault(sortBy, FIELD_CREATED_AT);
        Sort sort = sortDir.equalsIgnoreCase(SORT_ASC) ? Sort.by(actualSortField).ascending() : Sort.by(actualSortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Khoi tao dieu kien tim kiem dong dua tren cac tham so dau vao
        Specification<Company> spec = CompanySpecification.filterCompaniesForAdmin(
                searchName, searchCode, searchEmail, searchStatus, searchPlanCode);

        // Truy van du lieu theo dieu kien
        Page<Company> companiesPage = companyRepository.findAll(spec, pageable);

        Page<AdminCompanyResponse> dtoPage = companiesPage.map(this::mapToAdminResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public Tenant360Response getTenant360View(Integer companyId) {
        // Tim kiem cong ty hoac bao loi neu khong ton tai
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        // Tim goi cuoc dang hoat dong
        CompanySubscription sub = company.getSubscriptions().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        // Khoi tao cac gia tri mac dinh neu cong ty khong co goi cuoc
        String planCode = DEFAULT_PLAN_CODE;
        String planName = DEFAULT_PLAN_NAME;
        String subStatus = DEFAULT_SUB_STATUS;
        Integer maxUsers = 0;
        Integer maxProjects = 0;
        long maxStorageBytes = 0;
        BigDecimal price = BigDecimal.ZERO; 
        LocalDateTime start = null;
        LocalDateTime end = null;

        // Cap nhat thong so tu goi cuoc hien tai cua cong ty
        if (sub != null && sub.getPlan() != null) {
            planCode = sub.getPlan().getPlanCode();
            planName = sub.getPlan().getName();
            price = sub.getPlan().getMonthlyPrice();
            subStatus = sub.getStatus().toString();
            start = sub.getCurrentPeriodStart();
            end = sub.getCurrentPeriodEnd();
            
            maxUsers = sub.getPlan().getMaxUsers();
            maxProjects = sub.getPlan().getMaxProjects();
            
            // Tinh toan gioi han luu tru sang don vi Bytes de dong bo voi du lieu thuc te
            if (sub.getPlan().getMaxStorageGb() != UNLIMITED_VALUE) {
                maxStorageBytes = sub.getPlan().getMaxStorageGb() * BYTES_IN_GB;
            } else {
                maxStorageBytes = UNLIMITED_VALUE; 
            }
        }

        // Lay thong so su dung thuc te cua cong ty
        long currentMembers = companyMemberRepository.countByCompany_IdAndStatusNot(companyId, MemberStatus.REMOVED);
        long currentProjects = projectRepository.countByWorkspace_Company_IdAndStatusNot(companyId, ProjectStatus.CANCELLED); 
        long currentStorage = company.getCurrentStorageBytes() != null ? company.getCurrentStorageBytes() : 0;

        // Tinh toan cac co canh bao tinh trang dung luong/thoi gian danh cho Frontend
        boolean isGracePeriod = (end != null && end.isBefore(LocalDateTime.now())) 
                             || STATUS_PAST_DUE.equalsIgnoreCase(subStatus);

        boolean isUserLimitExceeded = (maxUsers != UNLIMITED_VALUE) && (currentMembers >= maxUsers);
        boolean isProjectLimitExceeded = (maxProjects != UNLIMITED_VALUE) && (currentProjects >= maxProjects);
        boolean isStorageLimitExceeded = (maxStorageBytes != UNLIMITED_VALUE) && (currentStorage >= maxStorageBytes);

        // Chuyen doi Entity sang DTO hoan chinh
        return buildTenant360Response(company, planCode, planName, price, subStatus, start, end,
                currentMembers, maxUsers, currentProjects, maxProjects, currentStorage, maxStorageBytes,
                isGracePeriod, isUserLimitExceeded, isProjectLimitExceeded, isStorageLimitExceeded);
    }

    @Override
    @Transactional
    public void changeCompanyStatus(Integer companyId, String newStatus) {
        // Tim cong ty can thay doi trang thai
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        // Kiem tra tinh hop le cua trang thai va cap nhat vao Database
        try {
            CompanyStatus statusEnum = CompanyStatus.valueOf(newStatus.toUpperCase());
            company.setStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(ERROR_INVALID_STATUS + newStatus);
        }
        
        companyRepository.save(company);
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---
    
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

        // Tim kiem goi cuoc dang hoat dong de map thong tin cho admin
        CompanySubscription sub = company.getSubscriptions().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElse(null);

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

    private Tenant360Response buildTenant360Response(Company company, String planCode, String planName, BigDecimal price,
                                                     String subStatus, LocalDateTime start, LocalDateTime end,
                                                     long currentMembers, Integer maxUsers, long currentProjects, Integer maxProjects,
                                                     long currentStorage, long maxStorageBytes, boolean isGracePeriod,
                                                     boolean isUserLimitExceeded, boolean isProjectLimitExceeded, boolean isStorageLimitExceeded) {
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
                .currentStorageBytes(currentStorage)
                .maxStorageBytes(maxStorageBytes)
                .isGracePeriod(isGracePeriod)
                .isUserLimitExceeded(isUserLimitExceeded)
                .isProjectLimitExceeded(isProjectLimitExceeded)
                .isStorageLimitExceeded(isStorageLimitExceeded)
                .build();
    }

    @Override
    public PageResponseDTO<TransactionHistoryResponse> getCompanyTransactionHistory(
            Integer companyId, com.quanlyduan.project_manager_api.model.common.enums.TransactionStatus status, 
            java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, 
            int page, int size, String sortBy, String sortDir) {

        // 1. Kiem tra cong ty co ton tai khong
        if (!companyRepository.existsById(companyId)) {
            throw new com.quanlyduan.project_manager_api.exception.ResourceNotFoundException("Company not found with ID: " + companyId);
        }

        // 2. Cau hinh phan trang va sap xep
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) 
                    ? org.springframework.data.domain.Sort.by(sortBy).ascending() 
                    : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        // 3. Goi repository lay data
        org.springframework.data.domain.Page<com.quanlyduan.project_manager_api.model.Transaction> transactionPage = 
                transactionRepository.filterTransactions(companyId, status, startDate, endDate, pageable);

        // 4. Map du lieu sang DTO
        java.util.List<TransactionHistoryResponse> content = transactionPage.getContent().stream()
                .map(this::mapToTransactionResponse)
                .collect(java.util.stream.Collectors.toList());

        // 5. Tra ve PageResponse
        return PageResponseDTO.<TransactionHistoryResponse>builder()
                .content(content)
                .pageSize(transactionPage.getNumber()) 
                .pageSize(transactionPage.getSize())   
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();
    }

    // Ham Helper ho tro Map du lieu (Dat o cuoi file Impl)
    private TransactionHistoryResponse mapToTransactionResponse(com.quanlyduan.project_manager_api.model.Transaction t) {
        return TransactionHistoryResponse.builder()
                .id(t.getId())
                .transactionCode(t.getTransactionCode())
                .gatewayTransactionId(t.getGatewayTransactionId())
                .planName(t.getPlan() != null ? t.getPlan().getName() : "Unknown")
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .billingCycle(t.getBillingCycle() != null ? t.getBillingCycle().name() : null)
                .paymentMethod(t.getPaymentMethod())
                .status(t.getStatus() != null ? t.getStatus().name() : "UNKNOWN")
                .paidAt(t.getPaidAt())
                .createdAt(t.getCreatedAt())
                .build();
    }
}