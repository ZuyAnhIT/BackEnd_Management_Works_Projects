package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.CreateEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateEpicRequest;
import com.quanlyduan.project_manager_api.dto.response.EpicResponse;

/**
 * Service quan ly toan bo vong doi cua Epic (Su thi/Muc tieu lon).
 * Epic dong vai tro la thung chua (Container) cho cac Story va Task, 
 * giup phan nhom cong viec theo cac tinh nang hoac giai doan lon cua Du an.
 */
public interface EpicService {

    // ======================================================
    // 1. TRA CUU VA TRICH XUAT (READ OPERATIONS)
    // ======================================================

    /**
     * Lay danh sach toan bo cac Epic thuoc ve mot Du an cu the.
     * Ho tro loc nhanh theo tu khoa tim kiem trong ten Epic.
     * * @param projectId ID dinh danh cua Du an
     * @param keyword Tu khoa tim kiem (vi du: ten hoac ma Epic)
     * @return Danh sach cac Epic phu hop voi tieu chi
     */
    List<EpicResponse> getEpicsByProject(Integer projectId, String keyword);

    /**
     * Truy xuat thong tin chi tiet cua mot Epic cu the.
     * Thuc hien kiem tra pham vi de dam bao Epic thuoc dung Du an dang xet.
     * * @param projectId ID Du an (dung de xac thuc pham vi truy cap)
     * @param epicId ID dinh danh cua Epic
     * @return DTO chua thong tin chi tiet va tien do cua Epic
     */
    EpicResponse getEpicDetails(Integer projectId, Integer epicId);

    // ======================================================
    // 2. KHOI TAO VA CAP NHAT (WRITE OPERATIONS)
    // ======================================================
    
    /**
     * Khoi tao mot Epic moi cho Du an.
     * Logic nghiep vu: Tu dong sinh ma dinh danh (Epic Code) va thiet lap trang thai OPEN.
     * * @param projectId ID Du an so huu Epic
     * @param request Du lieu thong tin khoi tao Epic
     * @return Thong tin Epic sau khi da duoc luu tru thanh cong
     */
    EpicResponse createEpic(Integer projectId, CreateEpicRequest request);

    /**
     * Cap nhat cac thong tin thay doi cua mot Epic (Partial Update).
     * * @param projectId ID Du an (dung de kiem tra tinh hop le)
     * @param epicId ID Epic can thay doi thong tin
     * @param request Cac truong thong tin can cap nhat (Ten, Mo ta, Mau sac, Ngay)
     * @return Thong tin Epic sau khi da cap nhat
     */
    EpicResponse updateEpic(Integer projectId, Integer epicId, UpdateEpicRequest request);

    /**
     * Loai bo mot Epic khoi he thong.
     * Logic nghiep vu: He thong se tu choi xoa neu Epic dang chua cac Task con de bao ve du lieu.
     * * @param projectId ID Du an
     * @param epicId ID Epic can loai bo
     */
    void deleteEpic(Integer projectId, Integer epicId);
}