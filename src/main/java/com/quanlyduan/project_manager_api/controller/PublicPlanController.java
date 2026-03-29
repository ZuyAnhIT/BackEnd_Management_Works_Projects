package com.quanlyduan.project_manager_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.MySubscriptionResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.plan.PublicPlanResponse;
import com.quanlyduan.project_manager_api.service.SubscriptionPlanService;

/**
 * Controller xử lý việc hiển thị thông tin các gói cước (Pricing Plans) công khai.
 * Phục vụ cho Landing Page và thông tin gói cước của khách hàng.
 */
@RestController
@RequestMapping("/api/plans")
@CrossOrigin("*")
public class PublicPlanController {

    // Khai báo các hằng số mặc định cho phân trang và sắp xếp
    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_SIZE = "10";
    private static final String DEFAULT_SORT_BY = "sortOrder";
    private static final String DEFAULT_SORT_DIR = "asc";

    // Khai báo các thông báo trả về (Response Messages)
    private static final String MSG_FETCH_PLANS_SUCCESS = "Fetched pricing plans successfully.";
    private static final String MSG_SEARCH_PLANS_SUCCESS = "Searched pricing plans successfully.";
    private static final String MSG_FETCH_DETAIL_SUCCESS = "Fetched pricing plan details successfully.";
    private static final String MSG_MY_SUBSCRIPTION_SUCCESS = "Fetched my subscription info successfully.";

    private final SubscriptionPlanService planService;

    // Khởi tạo thủ công để tiêm phụ thuộc (Dependency Injection)
    public PublicPlanController(SubscriptionPlanService planService) {
        this.planService = planService;
    }

    /**
     * Lấy danh sách bảng giá các gói cước hiển thị trên Landing Page.
     * Không yêu cầu xác thực người dùng.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<PublicPlanResponse>>> getPlans(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIR) String sortDir) {

        PageResponseDTO<PublicPlanResponse> response = planService.getPublicPlans(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_PLANS_SUCCESS, response));
    }

    /**
     * Tìm kiếm gói cước dựa trên tên (dành cho khách hàng lọc thông tin).
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDTO<PublicPlanResponse>>> searchPlans(
            @RequestParam(required = false) String searchName,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIR) String sortDir) {

        PageResponseDTO<PublicPlanResponse> response = planService.searchPublicPlans(searchName, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MSG_SEARCH_PLANS_SUCCESS, response));
    }

    /**
     * Lấy thông tin chi tiết của một gói cước cụ thể qua ID.
     */
    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<PublicPlanResponse>> getPlanById(@PathVariable Integer planId) {
        
        PublicPlanResponse response = planService.getPublicPlanById(planId);
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_DETAIL_SUCCESS, response));
    }

    /**
     * Lấy thông tin gói cước hiện tại và hạn mức sử dụng của công ty.
     * Yêu cầu quyền quản lý thanh toán của công ty.
     */
    @GetMapping("/my-subscription")
    @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:manage_billing')")
    public ResponseEntity<ApiResponse<MySubscriptionResponse>> getMySubscriptionInfo(
            @RequestParam Integer companyId) {
            
        MySubscriptionResponse response = planService.getMySubscriptionInfo(companyId);
        return ResponseEntity.ok(ApiResponse.success(MSG_MY_SUBSCRIPTION_SUCCESS, response));
    }
}