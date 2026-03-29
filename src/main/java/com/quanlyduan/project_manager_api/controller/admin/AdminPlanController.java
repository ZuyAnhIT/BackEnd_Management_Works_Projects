package com.quanlyduan.project_manager_api.controller.admin;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.request.plan.CreatePlanRequest;
import com.quanlyduan.project_manager_api.dto.request.plan.UpdatePlanRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.plan.PlanResponse;
import com.quanlyduan.project_manager_api.service.SubscriptionPlanService;

/**
 * Controller quản lý các gói cước (Subscription Plan) dành cho Quản trị viên hệ thống.
 */
@RestController
@RequestMapping("/api/admin/plans")
public class AdminPlanController {

    // Khai báo các hằng số mặc định cho phân trang và sắp xếp
    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_SIZE = "10";
    private static final String SORT_BY_ORDER = "sortOrder";
    private static final String SORT_BY_CREATED_AT = "createdAt";
    private static final String SORT_DIR_ASC = "asc";
    private static final String SORT_DIR_DESC = "desc";

    private final SubscriptionPlanService planService;

    // Khởi tạo thủ công để tiêm phụ thuộc thay vì dùng RequiredArgsConstructor
    public AdminPlanController(SubscriptionPlanService planService) {
        this.planService = planService;
    }

    /**
     * Tạo mới một gói cước hệ thống.
     */
    @PostMapping
    @PreAuthorize("@securityService.hasSystemPermission('plan:create')") 
    public ResponseEntity<ApiResponse<PlanResponse>> createPlan(
            @Valid @RequestBody CreatePlanRequest request) {
        
        PlanResponse response = planService.createPlan(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription plan created successfully.", response));
    }

    /**
     * Cập nhật thông tin của một gói cước hiện có.
     */
    @PutMapping("/{planId}")
    @PreAuthorize("@securityService.hasSystemPermission('plan:edit')") 
    public ResponseEntity<ApiResponse<PlanResponse>> updatePlan(
            @PathVariable Integer planId,
            @Valid @RequestBody UpdatePlanRequest request) {

        PlanResponse response = planService.updatePlan(planId, request);

        return ResponseEntity.ok(ApiResponse.success("Subscription plan updated successfully.", response));
    }

    /**
     * Lấy danh sách các gói cước có hỗ trợ phân trang.
     * Mặc định ưu tiên sắp xếp theo thứ tự hiển thị (sortOrder).
     */
    @GetMapping
    @PreAuthorize("@securityService.hasSystemPermission('plan:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<PlanResponse>>> getPlans(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = SORT_BY_ORDER) String sortBy, 
            @RequestParam(defaultValue = SORT_DIR_ASC) String sortDir) {

        PageResponseDTO<PlanResponse> response = planService.getPlans(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Fetched plans successfully.", response));
    }

    /**
     * Tìm kiếm gói cước theo nhiều tiêu chí (tên, mã gói, trạng thái).
     */
    @GetMapping("/search")
    @PreAuthorize("@securityService.hasSystemPermission('plan:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<PlanResponse>>> searchPlans(
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) String searchPlanCode,
            @RequestParam(required = false) Boolean searchStatus,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = SORT_BY_CREATED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {

        PageResponseDTO<PlanResponse> response = planService.searchPlans(
                searchName, searchPlanCode, searchStatus, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success("Searched plans successfully.", response));
    }

    /**
     * Lấy thông tin chi tiết của một gói cước cụ thể.
     */
    @GetMapping("/{planId}")
    @PreAuthorize("@securityService.hasSystemPermission('plan:view')")
    public ResponseEntity<ApiResponse<PlanResponse>> getPlanById(@PathVariable Integer planId) {
        
        PlanResponse response = planService.getPlanById(planId);
        
        return ResponseEntity.ok(ApiResponse.success("Fetched plan details successfully.", response));
    }
}