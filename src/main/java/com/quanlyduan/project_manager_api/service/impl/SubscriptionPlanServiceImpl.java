package com.quanlyduan.project_manager_api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.dto.request.plan.CreatePlanRequest;
import com.quanlyduan.project_manager_api.dto.request.plan.UpdatePlanRequest;
import com.quanlyduan.project_manager_api.dto.response.plan.PlanResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.SubscriptionPlan;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.SubscriptionPlanRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final SecurityService securityService;
    private final ObjectMapper objectMapper; // Dùng để parse JSON

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
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}