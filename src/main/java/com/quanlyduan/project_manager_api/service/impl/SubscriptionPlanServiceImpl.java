package com.quanlyduan.project_manager_api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.dto.request.plan.CreatePlanRequest;
import com.quanlyduan.project_manager_api.dto.request.plan.UpdatePlanRequest;
import com.quanlyduan.project_manager_api.dto.response.plan.PlanResponse;
import com.quanlyduan.project_manager_api.dto.response.plan.PublicPlanResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.SubscriptionPlan;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.SubscriptionPlanRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.quanlyduan.project_manager_api.repository.specification.SubscriptionPlanSpecification;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final SecurityService securityService;
    private final ObjectMapper objectMapper; 
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        
        // 1. Lấy thông tin Super Admin đang thực hiện thao tác
        User currentAdmin = securityService.getCurrentAuthenticatedUser();

        // 2. Validate mã gói
        if (planRepository.existsByPlanCode(request.getPlanCode())) {
            throw new BadRequestException("Plan code '" + request.getPlanCode() + "' already exists.");
        }

        // 3. Xử lý JsonNode features thành String để lưu DB
        String featuresString = null;
        if (request.getFeatures() != null) {
            try {
                featuresString = objectMapper.writeValueAsString(request.getFeatures());
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Invalid JSON format for features.");
            }
        }

        // 4. Build Entity
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

        // 5. Trả về Response
        return PlanResponse.builder()
                .id(savedPlan.getId())
                .planCode(savedPlan.getPlanCode())
                .name(savedPlan.getName())
                .monthlyPrice(savedPlan.getMonthlyPrice())
                .maxUsers(savedPlan.getMaxUsers())
                .maxWorkspaces(savedPlan.getMaxWorkspaces())
                .maxProjects(savedPlan.getMaxProjects())
                .maxStorageGb(savedPlan.getMaxStorageGb())
                .features(request.getFeatures()) // Trả lại nguyên gốc JSON cho Frontend
                .isActive(savedPlan.getIsActive())
                .sortOrder(savedPlan.getSortOrder())
                .createdAt(savedPlan.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public PlanResponse updatePlan(Integer planId, UpdatePlanRequest request) {
        
        SubscriptionPlan existingPlan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + planId));

        User currentAdmin = securityService.getCurrentAuthenticatedUser();

        // 1. Plan Code (Phải check trùng lặp nếu có đổi)
        if (request.getPlanCode() != null && !request.getPlanCode().trim().isEmpty() 
            && !existingPlan.getPlanCode().equals(request.getPlanCode())) {
            
            if (planRepository.existsByPlanCodeAndIdNot(request.getPlanCode(), planId)) {
                throw new BadRequestException("Plan code '" + request.getPlanCode() + "' is already in use by another plan.");
            }
            existingPlan.setPlanCode(request.getPlanCode());
        }

        // 2. Name & Description
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            existingPlan.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingPlan.setDescription(request.getDescription());
        }

        // 3. Prices
        if (request.getMonthlyPrice() != null) existingPlan.setMonthlyPrice(request.getMonthlyPrice());
        if (request.getYearlyPrice() != null) existingPlan.setYearlyPrice(request.getYearlyPrice());

        // 4. Limits & Quotas
        if (request.getMaxUsers() != null) existingPlan.setMaxUsers(request.getMaxUsers());
        if (request.getMaxWorkspaces() != null) existingPlan.setMaxWorkspaces(request.getMaxWorkspaces());
        if (request.getMaxProjects() != null) existingPlan.setMaxProjects(request.getMaxProjects());
        if (request.getMaxStorageGb() != null) existingPlan.setMaxStorageGb(request.getMaxStorageGb());

        // 5. Features (JSON Node)
        if (request.getFeatures() != null) {
            try {
                existingPlan.setFeatures(objectMapper.writeValueAsString(request.getFeatures()));
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Invalid JSON format for features.");
            }
        }

        // 6. Settings
        if (request.getIsActive() != null) existingPlan.setIsActive(request.getIsActive());
        if (request.getSortOrder() != null) existingPlan.setSortOrder(request.getSortOrder());

        // Ghi lại dấu vết người cập nhật
        existingPlan.setUpdatedBy(currentAdmin);

        SubscriptionPlan savedPlan = planRepository.save(existingPlan);

        // Trả về response (Ánh xạ các trường từ savedPlan)
        // ... (bạn dùng lại đoạn code return PlanResponse.builder()... như cũ nhé)
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
                .features(request.getFeatures() != null ? request.getFeatures() : 
                         (savedPlan.getFeatures() != null ? parseJsonString(savedPlan.getFeatures()) : null))
                .isActive(savedPlan.getIsActive())
                .sortOrder(savedPlan.getSortOrder())
                .createdAt(savedPlan.getCreatedAt())
                .updatedAt(savedPlan.getUpdatedAt())
                .build();
    }

    // Hàm tiện ích nhỏ để parse String từ DB về JsonNode cho Response (nếu cần)
    private JsonNode parseJsonString(String json) {
        // 👉 THÊM DÒNG NÀY: Chặn ngay nếu json là null hoặc rỗng
        if (json == null || json.trim().isEmpty()) {
            return null; 
        }
        
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return null; // Hoặc bạn có thể throw Exception tùy logic
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PlanResponse> getPlans(int page, int size, String sortBy, String sortDir) {
        // 1. Tạo đối tượng Pageable (Có Sort)
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. Gọi Repository lấy dữ liệu
        Page<SubscriptionPlan> plansPage = planRepository.findAll(pageable);

        // 3. Map sang DTO và trả về
        Page<PlanResponse> dtoPage = plansPage.map(this::mapToPlanResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PlanResponse> searchPlans(
            String searchName, String searchPlanCode, Boolean searchStatus, 
            int page, int size, String sortBy, String sortDir) {

        // 1. Tạo đối tượng Pageable
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. Tạo Specification
        Specification<SubscriptionPlan> spec = SubscriptionPlanSpecification.filterPlans(searchName, searchPlanCode, searchStatus);

        // 3. Lọc qua Repository
        Page<SubscriptionPlan> plansPage = planRepository.findAll(spec, pageable);

        // 4. Map sang DTO và trả về
        Page<PlanResponse> dtoPage = plansPage.map(this::mapToPlanResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // ==========================================
    // Hàm Helper để tái sử dụng việc Map Entity -> Response
    // ==========================================
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

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PublicPlanResponse> getPublicPlans(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Gọi hàm chỉ lấy gói Active
        Page<SubscriptionPlan> plansPage = planRepository.findByIsActiveTrue(pageable);

        Page<PublicPlanResponse> dtoPage = plansPage.map(this::mapToPublicPlanResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PublicPlanResponse> searchPublicPlans(String searchName, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Gọi Specification đã ép cứng isActive = true
        Specification<SubscriptionPlan> spec = SubscriptionPlanSpecification.filterPublicPlans(searchName);
        Page<SubscriptionPlan> plansPage = planRepository.findAll(spec, pageable);

        Page<PublicPlanResponse> dtoPage = plansPage.map(this::mapToPublicPlanResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // ==========================================
    // Hàm Helper ánh xạ sang Public DTO
    // ==========================================
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
                .features(parseJsonString(plan.getFeatures())) // Dùng lại hàm parse JSON cũ đã fix lỗi
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse getPlanById(Integer planId) {
        // Admin: Lấy ra mọi gói, kể cả gói isActive = false
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + planId));
        
        return mapToPlanResponse(plan); // Dùng lại hàm map của Admin
    }

    @Override
    @Transactional(readOnly = true)
    public PublicPlanResponse getPublicPlanById(Integer planId) {
        // Khách hàng: Chỉ lấy được gói đang mở bán. 
        // Nếu truyền ID của một gói đã khóa, hệ thống sẽ báo 404 Not Found như thể nó không tồn tại.
        SubscriptionPlan plan = planRepository.findByIdAndIsActiveTrue(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found or no longer active."));
        
        return mapToPublicPlanResponse(plan); // Dùng lại hàm map của Public
    }

    @Override
    @Transactional
    public void cancelActiveSubscription(Integer companyId) {
        // 1. Tìm công ty
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Công ty."));

        // 2. Lấy gói cước đang ACTIVE (Đang sử dụng) của công ty
        CompanySubscription activeSub = company.getSubscriptions().stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Công ty hiện không có gói cước nào đang hoạt động để hủy."));

        // 3. Kiểm tra xem khách đã bấm hủy trước đó chưa
        if (Boolean.TRUE.equals(activeSub.getCancelAtPeriodEnd())) {
            throw new BadRequestException("Gói cước này đã được yêu cầu hủy vào cuối kỳ từ trước rồi.");
        }

        // 4. BẬT CỜ HỦY VÀO CUỐI KỲ (Chuẩn SaaS)
        activeSub.setCancelAtPeriodEnd(true);

        // 5. Lưu xuống Database
        // (Do Entity Company đã map cascade = CascadeType.ALL với danh sách Subscriptions nên chỉ cần save Company)
        companyRepository.save(company);

        log.info("Khách hàng đã yêu cầu hủy gói [{}]. Gói sẽ tự động kết thúc vào ngày: {}", 
                 activeSub.getPlan().getName(), activeSub.getCurrentPeriodEnd());
    }
}