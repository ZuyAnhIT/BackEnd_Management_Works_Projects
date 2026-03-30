package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.plan.CreatePlanRequest;
import com.quanlyduan.project_manager_api.dto.request.plan.UpdatePlanRequest;
import com.quanlyduan.project_manager_api.dto.response.MySubscriptionResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.plan.PlanResponse;
import com.quanlyduan.project_manager_api.dto.response.plan.PublicPlanResponse;

/**
 * Service quan ly cac nghiep vu lien quan den Goi cuoc SaaS (Subscription Plan).
 * Cung cap cac tinh nang quan tri cho Super Admin va tra cuu cho Khach hang.
 */
public interface SubscriptionPlanService {

    // ======================================================
    // 1. QUAN TRI HE THONG (ADMIN OPERATIONS)
    // ======================================================

    /**
     * Khoi tao mot Goi cuoc moi tren he thong Worknet.
     * * @param request Thong tin chi tiet ve han muc va gia ca cua goi
     * @return Thong tin goi cuoc sau khi luu tru thanh cong
     */
    PlanResponse createPlan(CreatePlanRequest request);

    /**
     * Cap nhat thong so cua mot Goi cuoc hien co.
     * * @param planId ID dinh danh cua goi cuoc
     * @param request Cac truong thong tin can thay doi
     * @return Thong tin goi cuoc sau khi cap nhat
     */
    PlanResponse updatePlan(Integer planId, UpdatePlanRequest request);

    /**
     * Truy xuat danh sach toan bo cac goi cuoc voi day du thong so (Danh cho Admin).
     */
    PageResponseDTO<PlanResponse> getPlans(int page, int size, String sortBy, String sortDir);

    /**
     * Tim kiem nang cao danh sach goi cuoc dua tren Ten, Ma code hoac Trang thai.
     */
    PageResponseDTO<PlanResponse> searchPlans(
            String searchName, String searchPlanCode, Boolean searchStatus, 
            int page, int size, String sortBy, String sortDir);

    /**
     * Lay ho so chi tiet cua mot goi cuoc (Full Information).
     */
    PlanResponse getPlanById(Integer planId);

    // ======================================================
    // 2. NGHI KIEU KHACH HANG (PUBLIC & CUSTOMER OPERATIONS)
    // ======================================================

    /**
     * Lay danh sach cac goi cuoc dang hoat dong de khach hang tham khao.
     * Du lieu da duoc luoc bo cac thong tin quan tri nhay cam.
     */
    PageResponseDTO<PublicPlanResponse> getPublicPlans(int page, int size, String sortBy, String sortDir);

    /**
     * Tim kiem goi cuoc theo ten (Dinh dang cong khai cho khach hang).
     */
    PageResponseDTO<PublicPlanResponse> searchPublicPlans(
            String searchName, int page, int size, String sortBy, String sortDir);

    /**
     * Xem chi tiet mot goi cuoc cu the (Chi ap dung cho cac goi dang ACTIVE).
     */
    PublicPlanResponse getPublicPlanById(Integer planId);

    // ======================================================
    // 3. QUAN LY THUE BAO DOANH NGHIEP (SUBSCRIPTION MANAGEMENT)
    // ======================================================

    /**
     * Truy xuat thong tin ve goi cuoc hien tai ma doanh nghiep dang su dung.
     * * @param companyId ID dinh danh cua Cong ty
     * @return Thong tin thue bao, ngay het han va trang thai gia han
     */
    MySubscriptionResponse getMySubscriptionInfo(Integer companyId);

    /**
     * Yeu cau ngung gia han goi cuoc hien tai (Huy thue bao).
     * * @param companyId ID dinh danh cua Cong ty yeu cau huy
     */
    void cancelActiveSubscription(Integer companyId);
}