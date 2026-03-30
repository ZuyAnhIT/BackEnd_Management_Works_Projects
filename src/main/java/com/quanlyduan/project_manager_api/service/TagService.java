package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.CreateTagRequest;
import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTagRequest;
import com.quanlyduan.project_manager_api.dto.response.TagResponse;

/**
 * Service quan ly toan bo nghiep vu lien quan den The (Tag) trong Du an.
 * Tag duoc su dung de phan loai, gan nhan linh hoat va ho tro tim kiem nhanh cho cac Cong viec (Task).
 */
public interface TagService {

    // ======================================================
    // 1. QUAN LY THE (CRUD OPERATIONS)
    // ======================================================

    /**
     * Khoi tao mot Tag moi cho Du an.
     * * @param companyId ID Cong ty
     * @param workspaceId ID Khong gian lam viec
     * @param projectId ID Du an so huu Tag
     * @param request Thong tin cau hinh Tag (Ten, Ma mau)
     * @return DTO phan hoi sau khi tao thanh cong
     */
    TagResponse createTag(Integer companyId, Integer workspaceId, Integer projectId, CreateTagRequest request);

    /**
     * Cap nhat thong tin chi tiet cua mot Tag (Ten hoac Mau sac).
     * * @param tagId ID dinh danh cua Tag can sua
     * @param request Cac truong thong tin moi
     * @return DTO Tag sau khi da cap nhat
     */
    TagResponse updateTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId, UpdateTagRequest request);
    
    /**
     * Loai bo hoan toan mot Tag khoi Du an.
     * Logic nghiep vu: He thong se tu dong go lien ket cua Tag nay khoi tat ca cac Task lien quan.
     * * @param tagId ID Tag can xoa
     */
    void deleteTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId);

    // ======================================================
    // 2. TRA CUU VA LOC DU LIEU (READ & FILTER)
    // ======================================================

    /**
     * Truy xuat danh sach Tag cua mot Du an voi bo loc nang cao.
     * * @param filter DTO chua cac dieu kien loc (tu khoa, thoi gian tao, nguoi tao)
     * @return Danh sach cac TagResponse phu hop voi tieu chi
     */
    List<TagResponse> getProjectTags(Integer companyId, Integer workspaceId, Integer projectId, TagFilterRequest filter);

    // ======================================================
    // 3. QUAN LY LIEN KET TASK (TAG ASSIGNMENT)
    // ======================================================

    /**
     * Gan (Assign) mot Tag vao mot Cong viec cu the.
     * * @param taskId ID cua Cong viec can gan nhan
     * @param tagId ID cua Tag muon su dung
     * @return Danh sach toan bo cac Tag hien co cua Task sau khi thuc hien gan
     */
    List<TagResponse> assignTagToTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId);
    
    /**
     * Go bo (Unassign) mot Tag khoi Cong viec cu the.
     * * @param taskId ID cua Cong viec
     * @param tagId ID cua Tag muon go
     * @return Danh sach cac Tag con lai cua Task sau khi go
     */
    List<TagResponse> removeTagFromTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId);
}