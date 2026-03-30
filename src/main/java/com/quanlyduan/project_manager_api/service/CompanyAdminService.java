package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.company.AdminCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.company.Tenant360Response;

/**
 * Service danh rieng cho Admin he thong de quan ly cac Cong ty (Tenants).
 * Cung cap cac tinh nang tra cuu phan trang, tim kiem nang cao va quan tri tai nguyen doanh nghiep.
 */
public interface CompanyAdminService {

    // ======================================================
    // 1. QUAN LY DANH SACH (LIST MANAGEMENT)
    // ======================================================

    /**
     * Lay danh sach toan bo cac cong ty co trong he thong.
     * Ho tro phan trang va sap xep du lieu linh hoat.
     * * @param page So trang hien tai (bat dau tu 0)
     * @param size So luong ban ghi tren moi trang
     * @param sortBy Truong du lieu dung de sap xep
     * @param sortDir Huong sap xep (asc/desc)
     * @return Trang danh sach cac cong ty cho Admin
     */
    PageResponseDTO<AdminCompanyResponse> getCompanies(int page, int size, String sortBy, String sortDir);

    /**
     * Tim kiem cong ty dua tren nhieu tieu chi nang cao.
     * Cho phep loc theo ten, ma code, email chu so huu, trang thai hoac goi cuoc.
     * * @param searchName Ten cong ty can tim
     * @param searchCode Ma dinh danh cong ty
     * @param searchEmail Email cua nguoi dai dien
     * @param searchStatus Trang thai van hanh
     * @param searchPlanCode Ma goi cuoc hien tai
     * @return Ket qua tim kiem phan trang phu hop voi bo loc
     */
    PageResponseDTO<AdminCompanyResponse> searchCompanies(
            String searchName, String searchCode, String searchEmail, 
            String searchStatus, String searchPlanCode, 
            int page, int size, String sortBy, String sortDir);

    // ======================================================
    // 2. CHI TIET VA DIEU HANH (DETAIL & OPERATIONS)
    // ======================================================

    /**
     * Lay cai nhin tong the 360 do ve tai nguyen cua mot cong ty cu the.
     * Bao gom thong so su dung thuc te so voi han muc cua goi cuoc (Quota vs Usage).
     * * @param companyId ID cua cong ty can kiem tra
     * @return Thong tin chi tiet tai nguyen va thong so thue bao
     */
    Tenant360Response getTenant360View(Integer companyId);

    /**
     * Thay doi trang thai hoat dong cua mot cong ty trong he thong.
     * * @param companyId ID cua cong ty can cap nhat
     * @param newStatus Trang thai moi (Gia tri: ACTIVE, SUSPENDED, DELETED)
     */
    void changeCompanyStatus(Integer companyId, String newStatus);
}