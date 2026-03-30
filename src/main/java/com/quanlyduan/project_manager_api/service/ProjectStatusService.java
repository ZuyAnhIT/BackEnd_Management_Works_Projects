package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.CreateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.ReorderStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectStatusResponse;

/**
 * Service quan ly toan bo nghiep vu lien quan den Trang thai (Cot) cua Du an.
 * Cung cap cac API de thiet lap cau truc Bang (Board) nhu: them, sua, xoa va sap xep cot.
 */
public interface ProjectStatusService {

    // ======================================================
    // 1. TRA CUU VA HIEN THI (READ OPERATIONS)
    // ======================================================

    /**
     * Truy xuat danh sach toan bo cac Trang thai (Cot) thuoc ve mot Du an.
     * Du lieu tra ve duoc sap xep san theo thu tu (sortOrder) de hien thi tren giao dien Board.
     * * @param projectId ID dinh danh cua Du an
     * @return Danh sach cac DTO Trang thai da sap xep
     */
    List<ProjectStatusResponse> getProjectStatuses(Integer projectId);

    // ======================================================
    // 2. KHOI TAO VA CAP NHAT (WRITE OPERATIONS)
    // ======================================================

    /**
     * Khoi tao mot Trang thai (Cot) moi cho Du an.
     * Logic nghiep vu: He thong tu dong tinh toan va gan vi tri cuoi cung (sortOrder).
     * * @param projectId ID Du an so huu trang thai
     * @param request Thong tin cau hinh trang thai moi
     * @return DTO Trang thai vua duoc khoi tao
     */
    ProjectStatusResponse createStatus(Integer projectId, CreateProjectStatusRequest request);

    /**
     * Cap nhat thong tin chi tiet cua mot trang thai (Partial Update).
     * Cho phep thay doi: Ten, Ma mau, va Co danh dau hoan thanh (isDone).
     * * @param projectId ID Du an (dung de kiem tra tinh hop le/IDOR)
     * @param statusId ID dinh danh cua Trang thai can sua
     * @param request Cac truong thong tin can cap nhat
     * @return DTO Trang thai sau khi da cap nhat
     */
    ProjectStatusResponse updateStatus(Integer projectId, Integer statusId, UpdateStatusRequest request);
    
    /**
     * Thay doi thu tu sap xep cua cac Cot trong Du an (Drag & Drop Column).
     * Thuc hien cap nhat hang loat gia tri `sortOrder` dua tren danh sach ID moi.
     * * @param projectId ID Du an thuc hien sap xep
     * @param request Danh sach cac ID Trang thai theo thu tu moi tu trai sang phai
     */
    void reorderStatuses(Integer projectId, ReorderStatusRequest request);
    
    /**
     * Loai bo mot Trang thai (Cot) khoi Du an.
     * Logic nghiep vu: He thong chi cho phep xoa khi Cot khong chua bat ky Cong viec (Task) nao.
     * * @param projectId ID Du an (dung de bao mat pham vi)
     * @param statusId ID Trang thai can loai bo
     */
    void deleteStatus(Integer projectId, Integer statusId);
}