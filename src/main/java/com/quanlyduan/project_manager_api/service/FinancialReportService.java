package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.FinancialDashboardResponse;

public interface FinancialReportService {

    /**
     * Truy xuat thong tin tong quan ve tai chinh bao gom MRR, bieu do goi cuoc va giao dich gan nhat.
     * * @param targetYear Nam can thong ke (truyen null de lay nam hien tai)
     * @param targetMonth Thang can thong ke (truyen null de lay thang hien tai)
     * @return FinancialDashboardResponse Chua cac nhom du lieu da duoc tinh toan san
     */
    FinancialDashboardResponse getFinancialOverview(Integer targetYear, Integer targetMonth);
}