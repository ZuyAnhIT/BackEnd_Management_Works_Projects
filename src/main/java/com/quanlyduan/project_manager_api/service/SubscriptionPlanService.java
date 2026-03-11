package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreatePlanRequest;
import com.quanlyduan.project_manager_api.dto.response.PlanResponse;

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

}