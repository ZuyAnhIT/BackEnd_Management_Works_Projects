package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.plan.CreatePlanRequest;
import com.quanlyduan.project_manager_api.dto.request.plan.UpdatePlanRequest;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.plan.PlanResponse;

/**
 * Giao diện Service xử lý các nghiệp vụ liên quan đến Gói cước SaaS (Subscription Plan).
 * Dành riêng cho phân hệ Quản trị nền tảng (Super Admin).
 */
public interface SubscriptionPlanService {

    /**
     * Tạo một Gói cước mới trên hệ thống.
     *
     * @param request DTO chứa thông tin gói cước cần tạo
     * @return PlanResponse DTO chứa thông tin gói cước sau khi đã lưu thành công
     */
    PlanResponse createPlan(CreatePlanRequest request);

    /**
     * Cập nhật thông tin một Gói cước đã có.
     *
     * @param planId ID của gói cước cần sửa
     * @param request DTO chứa thông tin mới của gói cước
     * @return PlanResponse DTO chứa thông tin gói cước sau khi cập nhật
     */
    PlanResponse updatePlan(Integer planId, UpdatePlanRequest request);

    // Lấy danh sách phân trang cơ bản
    PageResponseDTO<PlanResponse> getPlans(int page, int size, String sortBy, String sortDir);

    // Lấy danh sách có tìm kiếm, lọc
    PageResponseDTO<PlanResponse> searchPlans(String searchName, String searchPlanCode, Boolean searchStatus, int page, int size, String sortBy, String sortDir);
}