package com.quanlyduan.project_manager_api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.dto.request.CreatePlanRequest;
import com.quanlyduan.project_manager_api.dto.response.PlanResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
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
}