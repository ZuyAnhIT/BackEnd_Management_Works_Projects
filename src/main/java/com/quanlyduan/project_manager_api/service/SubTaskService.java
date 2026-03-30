package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.CreateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;

/**
 * Service quan ly toan bo nghiep vu lien quan den Cong viec phu (SubTask).
 * Moi thao tac deu yeu cau xac thuc chuoi dinh danh (Company -> Workspace -> Project -> Task)
 * de dam bao tinh toan ven du lieu va bao mat tuyet doi cho Worknet.
 */
public interface SubTaskService {
    
    // ======================================================
    // 1. TRA CUU VA TRICH XUAT (READ OPERATIONS)
    // ======================================================
    
    /**
     * Lay danh sach tat ca cac SubTask thuoc ve mot Cong viec cha (Task).
     * * @param companyId ID dinh danh Cong ty
     * @param workspaceId ID Khong gian lam viec
     * @param projectId ID Du an chu quan
     * @param taskId ID Cong viec cha so huu SubTasks
     * @return Danh sach cac DTO phan hoi cua SubTask
     */
    List<SubTaskResponse> getSubTasks(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId);
    
    /**
     * Truy xuat thong tin chi tiet cua mot SubTask cu the.
     * Thuc hien kiem tra pham vi da tang (Cross-check) truoc khi tra ve du lieu.
     * * @param subTaskId ID dinh danh cua SubTask can xem
     * @return DTO chua noi dung chi tiet va trang thai cua SubTask
     */
    SubTaskResponse getSubTaskDetail(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId);

    // ======================================================
    // 2. KHOI TAO VA CAP NHAT (WRITE OPERATIONS)
    // ======================================================

    /**
     * Khoi tao mot SubTask moi va gan vao pham vi quan ly cua Task cha.
     * * @param taskId ID Cong viec cha can chia nho
     * @param request Du lieu khoi tao (Tieu de, Mo ta, Nguoi thuc hien)
     * @return Thong tin SubTask sau khi da duoc luu tru thanh cong
     */
    SubTaskResponse createSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, CreateSubTaskRequest request);
    
    /**
     * Cap nhat cac thong tin thay doi cua mot SubTask (Partial Update).
     * * @param subTaskId ID SubTask can thay doi thong tin
     * @param request Cac truong du lieu can cap nhat (Tieu de, Trang thai, Assignee)
     * @return Thong tin SubTask sau khi da cap nhat thanh cong
     */
    SubTaskResponse updateSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId, UpdateSubTaskRequest request);
    
    /**
     * Loai bo hoan toan mot SubTask khoi he thong.
     * * @param subTaskId ID SubTask can loai bo
     */
    void deleteSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId);
}