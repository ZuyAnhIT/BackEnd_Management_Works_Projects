package com.quanlyduan.project_manager_api.controller; 

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.MySubscriptionResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.plan.PublicPlanResponse;
import com.quanlyduan.project_manager_api.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PublicPlanController {

    private final SubscriptionPlanService planService;

    /**
     * API hiển thị Bảng giá trên Landing Page (Không yêu cầu đăng nhập, hoặc tùy bạn cấu hình Security)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<PublicPlanResponse>>> getPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy, 
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponseDTO<PublicPlanResponse> response = planService.getPublicPlans(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Fetched pricing plans successfully.", response));
    }

    /**
     * API tìm kiếm gói cước (Dành cho khách hàng lọc)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDTO<PublicPlanResponse>>> searchPlans(
            @RequestParam(required = false) String searchName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponseDTO<PublicPlanResponse> response = planService.searchPublicPlans(searchName, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Search pricing plans successfully.", response));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<PublicPlanResponse>> getPlanById(@PathVariable Integer planId) {
        
        PublicPlanResponse response = planService.getPublicPlanById(planId);
        
        return ResponseEntity.ok(ApiResponse.success("Fetched pricing plan details successfully.", response));
    }

    /**
     * API: Lấy thông tin gói cước và hạn mức sử dụng hiện tại của Công ty
     */
    @GetMapping("/my-subscription")
    @PreAuthorize("@securityService.hasCompanyPermission(#companyId, 'company:manage_billing')") // Hoặc 'company:view' tuỳ bạn quyết định ai được xem
    public ResponseEntity<ApiResponse<MySubscriptionResponse>> getMySubscriptionInfo(
            @RequestParam Integer companyId) {
            
        MySubscriptionResponse response = planService.getMySubscriptionInfo(companyId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin gói cước thành công.", response));
    }
}