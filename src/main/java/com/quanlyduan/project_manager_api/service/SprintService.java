package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.SprintDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.SprintResponse;

/**
 * Service quan ly toan bo vong doi cua Sprint trong quy trinh Agile/Scrum.
 * Xu ly cac trang thai van hanh, dieu chuyen cong viec giua Sprint va Backlog, 
 * cung nhu cung cap du lieu thong ke tien do.
 */
public interface SprintService {

    // ======================================================
    // 1. QUAN LY VONG DOI (LIFECYCLE MANAGEMENT)
    // ======================================================

    /**
     * Khoi tao mot Sprint moi cho Du an.
     * Ho tro tinh nang tao nhanh (Quick Create) de lap ke hoach linh hoat.
     * * @param projectId ID dinh danh cua Du an
     * @param request Thong tin cau hinh Sprint moi
     * @return DTO phan hoi sau khi tao thanh cong
     */
    SprintResponse createSprint(Integer projectId, CreateSprintRequest request); 

    /**
     * Cap nhat thong tin chi tiet cua Sprint (Partial Update).
     * Cho phep thay doi: Ten, Muc tieu, va Thoi gian du kien.
     * * @param projectId ID Du an (dung de kiem tra IDOR)
     * @param sprintId ID Sprint can chinh sua
     * @param request Cac truong thong tin can cap nhat
     * @return Thong tin Sprint sau khi thay doi
     */
    SprintResponse updateSprint(Integer projectId, Integer sprintId, UpdateSprintRequest request);

    /**
     * Kich hoat trang thai bat dau cho Sprint.
     * Chuyen trang thai tu NOT_STARTED sang IN_PROGRESS.
     * * @param projectId ID Du an
     * @param sprintId ID Sprint bat dau thuc thi
     * @return Thong tin Sprint voi trang thai moi
     */
    SprintResponse startSprint(Integer projectId, Integer sprintId);
    
    /**
     * Ket thuc mot chu ky Sprint.
     * Chuyen trang thai sang COMPLETED.
     * Logic nghiep vu: Tu dong dieu chuyen cac Task chua hoan thanh ve Backlog.
     * * @param projectId ID Du an
     * @param sprintId ID Sprint can dong lai
     * @return Thong tin Sprint sau khi hoan tat
     */
    SprintResponse completeSprint(Integer projectId, Integer sprintId);

    /**
     * Loai bo Sprint khoi he thong (Smart Delete).
     * Logic nghiep vu: Xoa vat ly neu Sprint rong va chua bat dau. 
     * Neu da co du lieu hoac dang chay, thuc hien xoa mem (Chuyen sang CANCELLED).
     * * @param projectId ID Du an
     * @param sprintId ID Sprint can loai bo
     */
    void deleteSprint(Integer projectId, Integer sprintId);

    // ======================================================
    // 2. TRICH XUAT DU LIEU (READ OPERATIONS)
    // ======================================================

    /**
     * Lay danh sach cac Sprint thuoc pham vi mot Du an.
     * * @param projectId ID Du an can tra cuu
     * @param status Bo loc theo trang thai (vi du: "COMPLETED", "IN_PROGRESS")
     * @return Danh sach cac ban ghi phu hop
     */
    List<SprintResponse> getSprintsByProject(Integer projectId, String status); 

    /**
     * Xem chi tiet noi dung ben trong mot Sprint.
     * Cung cap danh sach Task kem theo cac chi so quan trong (Story Points, Task Count).
     * * @param projectId ID Du an (dung de xac thuc quyen truy cap)
     * @param sprintId ID Sprint can xem chi tiet
     * @return Ho so chi tiet va thong ke cua Sprint
     */
    SprintDetailsResponse getSprintDetails(Integer projectId, Integer sprintId);

    // ======================================================
    // 3. HO TRO BAO MAT (SECURITY HELPERS)
    // ======================================================

    /**
     * Truy xuat nhanh ID Du an cha tu mot Sprint ID cu the.
     * Thuong duoc su dung de kiem tra quyen han thua ke (Inherited Permissions).
     * * @param sprintId ID cua Sprint
     * @return ID dinh danh cua Du an chu quan
     */
    Integer getProjectIdBySprint(Integer sprintId);
}