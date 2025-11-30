// File: src/main/java/com/quanlyduan/project_manager_api/service/TaskAttachmentService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến Tệp đính kèm (Attachment) trong Task.
 */
public interface TaskAttachmentService {
    
    // ========================================================================
    // 1. LƯU TRỮ VÀ TẠO MỚI (WRITE)
    // ========================================================================

    /**
     * Lưu file vật lý vào hệ thống và tạo bản ghi đính kèm cho Task.
     *
     * @param taskId ID của Task mà tệp tin được đính kèm.
     * @param file Dữ liệu tệp tin (MultipartFile)
     * @param uploaderId ID của người tải lên
     * @return DTO của tệp đính kèm vừa tạo.
     * @throws IOException Nếu xảy ra lỗi I/O trong quá trình lưu file.
     */
    TaskAttachmentResponse storeAttachment(Integer taskId, MultipartFile file, Integer uploaderId) throws IOException;

    // ========================================================================
    // 2. LẤY DANH SÁCH (READ)
    // ========================================================================

    /**
     * Lấy danh sách tất cả các tệp đính kèm của một Task.
     *
     * @param taskId ID của Task
     * @return Danh sách DTO các tệp đính kèm.
     */
    List<TaskAttachmentResponse> getAttachmentsForTask(Integer taskId);
}