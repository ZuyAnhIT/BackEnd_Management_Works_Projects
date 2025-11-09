// File: src/main/java/com/quanlyduan/project_manager_api/service/TaskAttachmentService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface TaskAttachmentService {
    
    // US11
    TaskAttachmentResponse storeAttachment(Integer taskId, MultipartFile file, Integer uploaderId) throws IOException;
    List<TaskAttachmentResponse> getAttachmentsForTask(Integer taskId);
}
