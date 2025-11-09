package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

// Đây là Interface (hợp đồng)
public interface TaskAttachmentService {

    /**
     * Nghiệp vụ 1: Lấy danh sách tệp tin cho một Task.
     * (Hàm này sẽ dùng File 1, 2, và 4)
     */
    List<TaskAttachmentResponse> getAttachmentsForTask(Integer taskId);

    /**
     * Nghiệp vụ 2: Lưu một tệp tin mới (Upload).
     */
    TaskAttachmentResponse storeAttachment(Integer taskId, MultipartFile file, Integer uploaderId) throws IOException;

    /**
     * Nghiệp vụ 3: Xóa một tệp tin.
     * (Thêm logic kiểm tra quyền của uploaderId hoặc project manager)
     */
    void deleteAttachment(Integer attachmentId, Integer uploaderId);

}
