package com.quanlyduan.project_manager_api.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.dto.request.plan.CreatePlanRequest;
import com.quanlyduan.project_manager_api.dto.request.plan.UpdatePlanRequest;
import com.quanlyduan.project_manager_api.dto.response.MySubscriptionResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.plan.PlanResponse;
import com.quanlyduan.project_manager_api.dto.response.plan.PublicPlanResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.SubscriptionPlan;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.SubscriptionPlanRepository;
import com.quanlyduan.project_manager_api.repository.specification.SubscriptionPlanSpecification;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.SubscriptionPlanService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ERROR_PLAN_CODE_EXISTS = "Plan code '%s' already exists.";
    public static final String ERROR_PLAN_CODE_IN_USE = "Plan code '%s' is already in use by another plan.";
    public static final String ERROR_INVALID_JSON_FEATURES = "Invalid JSON format for features.";
    public static final String ERROR_PLAN_NOT_FOUND = "Subscription plan not found with ID: ";
    public static final String ERROR_PLAN_NOT_ACTIVE = "Subscription plan not found or no longer active.";
    public static final String ERROR_COMPANY_NOT_FOUND = "Không tìm thấy Công ty.";
    public static final String ERROR_NO_ACTIVE_SUB_TO_CANCEL = "Công ty hiện không có gói cước nào đang hoạt động để hủy.";
    public static final String ERROR_SUB_ALREADY_CANCELLED = "Gói cước này đã được yêu cầu hủy vào cuối kỳ từ trước rồi.";

    public static final String LOG_CANCEL_SUB = "Khách hàng đã yêu cầu hủy gói [{}]. Gói sẽ tự động kết thúc vào ngày: {}";

    public static final String STATUS_NONE = "NONE";
    public static final String LABEL_NO_PLAN = "Chưa đăng ký gói";

    public static final long BYTES_PER_GB = 1073741824L;
    public static final int UNLIMITED_QUOTA = -1;

    // Khai bao cac bien phu thuoc
    private final SubscriptionPlanRepository planRepository;
    private final SecurityService securityService;
    private final ObjectMapper objectMapper; 
    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final ProjectRepository projectRepository;

    // Constructor khoi tao thu cong thay the cho @RequiredArgsConstructor
    public SubscriptionPlanServiceImpl(SubscriptionPlanRepository planRepository,
                                       SecurityService securityService,
                                       ObjectMapper objectMapper,
                                       CompanyRepository companyRepository,
                                       CompanyMemberRepository companyMemberRepository,
                                       ProjectRepository projectRepository) {
        this.planRepository = planRepository;
        this.securityService = securityService;
        this.objectMapper = objectMapper;
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.projectRepository = projectRepository;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        // Lay thong tin quan tri vien dang thuc hien thao tac
        User currentAdmin = securityService.getCurrentAuthenticatedUser();

        // Kiem tra tinh duy nhat cua ma goi cuoc
        if (planRepository.existsByPlanCode(request.getPlanCode())) {
            throw new BadRequestException(String.format(ERROR_PLAN_CODE_EXISTS, request.getPlanCode()));
        }

        // Chuyen doi danh sach tinh nang thanh chuoi JSON de luu vao co so du lieu
        String featuresString = null;
        if (request.getFeatures() != null) {
            try {
                featuresString = objectMapper.writeValueAsString(request.getFeatures());
            } catch (JsonProcessingException e) {
                throw new BadRequestException(ERROR_INVALID_JSON_FEATURES);
            }
        }

        // Khoi tao va luu goi cuoc moi
        SubscriptionPlan newPlan = SubscriptionPlan.builder()
                .planCode(request.getPlanCode())
                .name(request.getName())
                .description(request.getDescription())
                .monthlyPrice(request.getMonthlyPrice())
                .yearlyPrice(request.getYearlyPrice())
                .maxUsers(request.getMaxUsers())
                .maxWorkspaces(request.getMaxWorkspaces())
                .maxProjects(request.getMaxProjects())
                .maxStorageGb(request.getMaxStorageGb())
                .features(featuresString)
                .isActive(request.getIsActive())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .createdBy(currentAdmin)
                .updatedBy(currentAdmin)
                .build();

        SubscriptionPlan savedPlan = planRepository.save(newPlan);

        // Goi ham ho tro de tra ve ket qua bao gom JSON goc
        return buildPlanResponseWithRawFeatures(savedPlan, request.getFeatures());
    }

    @Override
    @Transactional
    public PlanResponse updatePlan(Integer planId, UpdatePlanRequest request) {
        // Tim kiem goi cuoc can cap nhat
        SubscriptionPlan existingPlan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PLAN_NOT_FOUND + planId));

        User currentAdmin = securityService.getCurrentAuthenticatedUser();

        // Kiem tra va cap nhat ma goi cuoc neu can
        if (request.getPlanCode() != null && !request.getPlanCode().trim().isEmpty() 
            && !existingPlan.getPlanCode().equals(request.getPlanCode())) {
            
            if (planRepository.existsByPlanCodeAndIdNot(request.getPlanCode(), planId)) {
                throw new BadRequestException(String.format(ERROR_PLAN_CODE_IN_USE, request.getPlanCode()));
            }
            existingPlan.setPlanCode(request.getPlanCode());
        }

        // Cap nhat ten va mo ta
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            existingPlan.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingPlan.setDescription(request.getDescription());
        }

        // Cap nhat thong tin gia ca
        if (request.getMonthlyPrice() != null) {
            existingPlan.setMonthlyPrice(request.getMonthlyPrice());
        }
        if (request.getYearlyPrice() != null) {
            existingPlan.setYearlyPrice(request.getYearlyPrice());
        }

        // Cap nhat cac han muc
        if (request.getMaxUsers() != null) {
            existingPlan.setMaxUsers(request.getMaxUsers());
        }
        if (request.getMaxWorkspaces() != null) {
            existingPlan.setMaxWorkspaces(request.getMaxWorkspaces());
        }
        if (request.getMaxProjects() != null) {
            existingPlan.setMaxProjects(request.getMaxProjects());
        }
        if (request.getMaxStorageGb() != null) {
            existingPlan.setMaxStorageGb(request.getMaxStorageGb());
        }

        // Cap nhat danh sach tinh nang bang chuoi JSON
        if (request.getFeatures() != null) {
            try {
                existingPlan.setFeatures(objectMapper.writeValueAsString(request.getFeatures()));
            } catch (JsonProcessingException e) {
                throw new BadRequestException(ERROR_INVALID_JSON_FEATURES);
            }
        }

        // Cap nhat trang thai hoat dong va vi tri sap xep
        if (request.getIsActive() != null) {
            existingPlan.setIsActive(request.getIsActive());
        }
        if (request.getSortOrder() != null) {
            existingPlan.setSortOrder(request.getSortOrder());
        }

        // Ghi nhan nguoi thuc hien cap nhat
        existingPlan.setUpdatedBy(currentAdmin);

        SubscriptionPlan savedPlan = planRepository.save(existingPlan);

        // Uu tien tra ve JSON dau vao neu co de giu nguyen dinh dang, nguoc lai phan tich tu DB
        JsonNode responseFeatures = request.getFeatures() != null ? request.getFeatures() : 
                                    (savedPlan.getFeatures() != null ? parseJsonString(savedPlan.getFeatures()) : null);

        return buildPlanResponseWithRawFeatures(savedPlan, responseFeatures);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PlanResponse> getPlans(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SubscriptionPlan> plansPage = planRepository.findAll(pageable);
        Page<PlanResponse> dtoPage = plansPage.map(this::mapToPlanResponse);
        
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PlanResponse> searchPlans(
            String searchName, String searchPlanCode, Boolean searchStatus, 
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<SubscriptionPlan> spec = SubscriptionPlanSpecification.filterPlans(searchName, searchPlanCode, searchStatus);
        Page<SubscriptionPlan> plansPage = planRepository.findAll(spec, pageable);

        Page<PlanResponse> dtoPage = plansPage.map(this::mapToPlanResponse);
        
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PublicPlanResponse> getPublicPlans(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Chi lay danh sach cac goi cuoc dang duoc mo ban
        Page<SubscriptionPlan> plansPage = planRepository.findByIsActiveTrue(pageable);
        Page<PublicPlanResponse> dtoPage = plansPage.map(this::mapToPublicPlanResponse);
        
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PublicPlanResponse> searchPublicPlans(String searchName, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Tim kiem goi cuoc theo ten nhung bat buoc phai dang hoat dong
        Specification<SubscriptionPlan> spec = SubscriptionPlanSpecification.filterPublicPlans(searchName);
        Page<SubscriptionPlan> plansPage = planRepository.findAll(spec, pageable);

        Page<PublicPlanResponse> dtoPage = plansPage.map(this::mapToPublicPlanResponse);
        
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse getPlanById(Integer planId) {
        // Cung cap toan bo thong tin chi tiet cho quan tri vien
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PLAN_NOT_FOUND + planId));
        
        return mapToPlanResponse(plan); 
    }

    @Override
    @Transactional(readOnly = true)
    public PublicPlanResponse getPublicPlanById(Integer planId) {
        // Chi cung cap thong tin neu goi cuoc dang duoc ban cho nguoi dung thuong
        SubscriptionPlan plan = planRepository.findByIdAndIsActiveTrue(planId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PLAN_NOT_ACTIVE));
        
        return mapToPublicPlanResponse(plan); 
    }

    @Override
    @Transactional
    public void cancelActiveSubscription(Integer companyId) {
        // Tim kiem cong ty
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        // Tim kiem goi cuoc dang hoat dong hien tai
        CompanySubscription activeSub = company.getSubscriptions().stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new BadRequestException(ERROR_NO_ACTIVE_SUB_TO_CANCEL));

        // Kiem tra tinh trang huy truoc do
        if (Boolean.TRUE.equals(activeSub.getCancelAtPeriodEnd())) {
            throw new BadRequestException(ERROR_SUB_ALREADY_CANCELLED);
        }

        // Kich hoat che do huy vao cuoi ky (Theo tieu chuan he thong SaaS)
        activeSub.setCancelAtPeriodEnd(true);
        companyRepository.save(company);

        log.info(LOG_CANCEL_SUB, activeSub.getPlan().getName(), activeSub.getCurrentPeriodEnd());
    }

    @Override
    @Transactional(readOnly = true)
    public MySubscriptionResponse getMySubscriptionInfo(Integer companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        // Tim goi cuoc hoat dong hoac qua han de thong ke
        CompanySubscription activeSub = company.getSubscriptions().stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE || sub.getStatus() == SubscriptionStatus.PAST_DUE)
                .findFirst()
                .orElse(null);

        if (activeSub == null || activeSub.getPlan() == null) {
            return MySubscriptionResponse.builder()
                    .planName(LABEL_NO_PLAN)
                    .subscriptionStatus(STATUS_NONE)
                    .build();
        }

        // Thong ke so luong tai nguyen thuc te ma cong ty dang su dung
        long currentMembers = companyMemberRepository.countByCompany_IdAndStatusNot(companyId, MemberStatus.REMOVED);
        long currentProjects = projectRepository.countByWorkspace_Company_IdAndStatusNot(companyId, ProjectStatus.CANCELLED);
        long currentStorage = company.getCurrentStorageBytes() != null ? company.getCurrentStorageBytes() : 0;
        
        // Quy doi tu don vi GB sang Bytes
        long maxStorageBytes = activeSub.getPlan().getMaxStorageGb() == UNLIMITED_QUOTA 
                ? UNLIMITED_QUOTA 
                : activeSub.getPlan().getMaxStorageGb() * BYTES_PER_GB; 

        return MySubscriptionResponse.builder()
                .planName(activeSub.getPlan().getName())
                .planCode(activeSub.getPlan().getPlanCode())
                .monthlyPrice(activeSub.getPlan().getMonthlyPrice())
                .yearlyPrice(activeSub.getPlan().getYearlyPrice())
                
                .subscriptionStatus(activeSub.getStatus().toString())
                .currentPeriodStart(activeSub.getCurrentPeriodStart())
                .currentPeriodEnd(activeSub.getCurrentPeriodEnd())
                .isCancelAtPeriodEnd(Boolean.TRUE.equals(activeSub.getCancelAtPeriodEnd()))
                
                .currentMembers(currentMembers)
                .maxMembers(activeSub.getPlan().getMaxUsers())
                
                .currentProjects(currentProjects)
                .maxProjects(activeSub.getPlan().getMaxProjects())
                
                .currentStorageBytes(currentStorage)
                .maxStorageBytes(maxStorageBytes)
                .build();
    }

    // --- CAC HAM PRIVATE HO TRO NGHIEP VU ---

    private JsonNode parseJsonString(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null; 
        }
        
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return null; 
        }
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private PlanResponse buildPlanResponseWithRawFeatures(SubscriptionPlan savedPlan, JsonNode features) {
        return PlanResponse.builder()
                .id(savedPlan.getId())
                .planCode(savedPlan.getPlanCode())
                .name(savedPlan.getName())
                .description(savedPlan.getDescription())
                .monthlyPrice(savedPlan.getMonthlyPrice())
                .yearlyPrice(savedPlan.getYearlyPrice())
                .maxUsers(savedPlan.getMaxUsers())
                .maxWorkspaces(savedPlan.getMaxWorkspaces())
                .maxProjects(savedPlan.getMaxProjects())
                .maxStorageGb(savedPlan.getMaxStorageGb())
                .features(features)
                .isActive(savedPlan.getIsActive())
                .sortOrder(savedPlan.getSortOrder())
                .createdAt(savedPlan.getCreatedAt())
                .updatedAt(savedPlan.getUpdatedAt())
                .build();
    }

    private PlanResponse mapToPlanResponse(SubscriptionPlan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .planCode(plan.getPlanCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .maxUsers(plan.getMaxUsers())
                .maxWorkspaces(plan.getMaxWorkspaces())
                .maxProjects(plan.getMaxProjects())
                .maxStorageGb(plan.getMaxStorageGb())
                .features(parseJsonString(plan.getFeatures()))
                .isActive(plan.getIsActive())
                .sortOrder(plan.getSortOrder())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private PublicPlanResponse mapToPublicPlanResponse(SubscriptionPlan plan) {
        return PublicPlanResponse.builder()
                .id(plan.getId())
                .planCode(plan.getPlanCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .maxUsers(plan.getMaxUsers())
                .maxWorkspaces(plan.getMaxWorkspaces())
                .maxProjects(plan.getMaxProjects())
                .maxStorageGb(plan.getMaxStorageGb())
                .features(parseJsonString(plan.getFeatures())) 
                .build();
    }
}