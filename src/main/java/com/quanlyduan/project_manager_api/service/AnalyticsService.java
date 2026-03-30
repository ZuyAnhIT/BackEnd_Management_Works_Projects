package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.AssigneeRecommendationRequest;
import com.quanlyduan.project_manager_api.dto.response.AssigneeRecommendationResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectForecastResponse;
import com.quanlyduan.project_manager_api.dto.response.StandupReportResponse;

/**
 * Service chuyen xu ly cac logic tinh toan va phan tich du lieu chuyen sau.
 * Ho tro tinh nang goi y nhan su, du bao tien do va bao cao Daily Standup cho AI Chatbot.
 */
public interface AnalyticsService {

    // ======================================================
    // 1. PHAN TICH NHAN SU (PEOPLE ANALYTICS)
    // ======================================================

    /**
     * Phan tich va goi y nguoi thuc hien phu hop nhat cho mot cong viec moi.
     * He thong dua tren ky nang, lich su hoan thanh va tai cong viec (Workload).
     * * @param projectId ID cua du an dang xet
     * @param request Thong tin chi tiet ve cong viec du kien tao
     * @return Danh sach ung vien duoc xep hang theo diem so phu hop
     */
    List<AssigneeRecommendationResponse> getAssigneeRecommendations(Integer projectId, AssigneeRecommendationRequest request);

    // ======================================================
    // 2. DU BAO & TIEN DO (FORECASTING & PROGRESS)
    // ======================================================

    /**
     * Du bao thoi diem hoan thanh du an dua tren du lieu lich su (Velocity).
     * Cung cap 3 kich ban: Best Case, Most Likely, va Worst Case.
     * * @param projectId ID cua du an can du bao
     * @return Thong tin du bao tien do chi tiet
     */
    ProjectForecastResponse getProjectForecast(Integer projectId);

    // ======================================================
    // 3. BAO CAO TUONG TAC (OPERATIONAL REPORTS)
    // ======================================================

    /**
     * Tong hop du lieu phuc vu buoi hop Daily Standup.
     * Thong ke cac cong viec da hoan thanh hom qua va ke hoach cho hom nay.
     * * @param projectId ID cua du an can lay bao cao
     * @return Du lieu bao cao Standup theo tung thanh vien
     */
    StandupReportResponse getDailyStandupReport(Integer projectId);
}