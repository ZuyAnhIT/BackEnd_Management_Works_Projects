package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.CustomerHealthDashboardResponse;

/**
 * Service quản lý các nghiệp vụ báo cáo, thống kê liên quan đến sức khỏe khách hàng (Tenant/Customer Health).
 * Cung cấp các chỉ số quan trọng cho mô hình SaaS như: Lượng khách hàng đang hoạt động, Tỷ lệ rời bỏ (Churn Rate), và Phân bổ gói cước.
 */
public interface CustomerHealthReportService {

    /**
     * Truy xuất thông tin tổng quan về tình trạng khách hàng trong một khoảng thời gian cụ thể.
     * So sánh dữ liệu của tháng được chọn với tháng liền kề trước đó để đưa ra tỷ lệ tăng trưởng.
     *
     * @param targetYear  Năm cần thống kê (truyền null để mặc định lấy năm hiện tại)
     * @param targetMonth Tháng cần thống kê (truyền null để mặc định lấy tháng hiện tại)
     * @return CustomerHealthDashboardResponse Chứa các nhóm dữ liệu đã được tính toán (Active Tenants, New vs Churn, Plan Distribution)
     */
    CustomerHealthDashboardResponse getCustomerHealthOverview(Integer targetYear, Integer targetMonth);

}