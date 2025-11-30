// File: src/main/java/com/quanlyduan/project_manager_api/service/TagService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateTagRequest;
import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTagRequest;
import com.quanlyduan.project_manager_api.dto.response.TagResponse;
import java.util.List;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến Thẻ (Tag) trong Dự án.
 * Tag dùng để phân loại và gắn nhãn cho Task.
 */
public interface TagService {

    // ========================================================================
    // 1. NHÓM TẠO/CẬP NHẬT/XÓA (CRUD)
    // ========================================================================

    /**
     * Tạo một Tag mới cho Dự án.
     */
    TagResponse createTag(Integer companyId, Integer workspaceId, Integer projectId, CreateTagRequest request);

    /**
     * Cập nhật thông tin chi tiết của một Tag.
     */
    TagResponse updateTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId, UpdateTagRequest request);
    
    /**
     * Xóa một Tag khỏi Dự án.
     */
    void deleteTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId);


    // ========================================================================
    // 2. NHÓM XEM & LỌC (READ & FILTER)
    // ========================================================================

    /**
     * Lấy danh sách Tag của một Dự án (Có tích hợp bộ lọc nâng cao).
     * @param filter DTO chứa các điều kiện lọc (keyword, ngày tạo, người tạo...).
     * @return Danh sách các TagResponse khớp với tiêu chí.
     */
    List<TagResponse> getProjectTags(Integer companyId, Integer workspaceId, Integer projectId, TagFilterRequest filter);

    // ========================================================================
    // 3. NHÓM GÁN/GỠ LIÊN KẾT (ASSIGNMENT)
    // ========================================================================

    /**
     * Gán một Tag vào một Task cụ thể.
     * @param taskId ID của Task.
     * @param tagId ID của Tag muốn gán.
     * @return Danh sách các Tag hiện tại của Task sau khi gán.
     */
    List<TagResponse> assignTagToTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId);
    
    /**
     * Gỡ (Unassign) một Tag khỏi một Task cụ thể.
     * @param taskId ID của Task.
     * @param tagId ID của Tag muốn gỡ.
     * @return Danh sách các Tag hiện tại của Task sau khi gỡ.
     */
    List<TagResponse> removeTagFromTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId);
}