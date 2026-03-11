package com.quanlyduan.project_manager_api.controller.admin;
import com.quanlyduan.project_manager_api.dto.request.plan.CreatePlanRequest;
import com.quanlyduan.project_manager_api.dto.request.plan.UpdatePlanRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.plan.PlanResponse;
import com.quanlyduan.project_manager_api.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/plans")
@RequiredArgsConstructor
public class AdminPlanController {

    private final SubscriptionPlanService planService;

    @PostMapping
    @PreAuthorize("@securityService.hasSystemPermission('plan:create')") 
    public ResponseEntity<ApiResponse<PlanResponse>> createPlan(
            @Valid @RequestBody CreatePlanRequest request) {
        
        PlanResponse response = planService.createPlan(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription plan created successfully.", response));
    }

    @PutMapping("/{planId}")
    @PreAuthorize("@securityService.hasSystemPermission('plan:edit')") 
    public ResponseEntity<ApiResponse<PlanResponse>> updatePlan(
            @PathVariable Integer planId,
            @Valid @RequestBody UpdatePlanRequest request) {

        PlanResponse response = planService.updatePlan(planId, request);

        return ResponseEntity.ok(ApiResponse.success("Subscription plan updated successfully.", response));
    }

    @GetMapping
    @PreAuthorize("@securityService.hasSystemPermission('plan:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<PlanResponse>>> getPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy, // Ưu tiên sắp xếp theo sortOrder
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponseDTO<PlanResponse> response = planService.getPlans(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Fetched plans successfully.", response));
    }

    @GetMapping("/search")
    @PreAuthorize("@securityService.hasSystemPermission('plan:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<PlanResponse>>> searchPlans(
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) String searchPlanCode,
            @RequestParam(required = false) Boolean searchStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponseDTO<PlanResponse> response = planService.searchPlans(
                searchName, searchPlanCode, searchStatus, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success("Search plans successfully.", response));
    }
}