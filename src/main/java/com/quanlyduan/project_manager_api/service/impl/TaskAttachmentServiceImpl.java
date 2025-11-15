// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/TaskAttachmentServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.TaskAttachment;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.TaskAttachmentRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.service.TaskAttachmentService;
// import lombok.RequiredArgsConstructor; // Đã xóa
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskAttachmentServiceImpl implements TaskAttachmentService {

    @Value("${app.upload.dir:uploads}") 
    private String uploadDir;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAttachmentRepository attachmentRepository;
    
    // *** THÊM CONSTRUCTOR THỦ CÔNG ***
    public TaskAttachmentServiceImpl(TaskRepository taskRepository, 
                                     UserRepository userRepository, 
                                     TaskAttachmentRepository attachmentRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    @Transactional
    public TaskAttachmentResponse storeAttachment(Integer taskId, MultipartFile file, Integer uploaderId) throws IOException {
        
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác vụ"));
        User uploader = userRepository.findById(uploaderId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // (Đây là logic lưu file cục bộ, thực tế nên dùng S3, MinIO...)
        Path dirPath = Paths.get(uploadDir, "task-" + taskId);
        Files.createDirectories(dirPath);
        
        String originalFileName = file.getOriginalFilename();
        String storedFileName = System.currentTimeMillis() + "_" + originalFileName;
        Path filePath = dirPath.resolve(storedFileName);

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        TaskAttachment attachment = TaskAttachment.builder()
            .task(task)
            .fileName(originalFileName)
            .filePath(filePath.toString()) // Lưu đường dẫn vật lý
            .fileType(file.getContentType())
            .fileSize(file.getSize())
            .uploadedBy(uploader)
            .build();
            
        TaskAttachment saved = attachmentRepository.save(attachment);
        return mapToAttachmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAttachmentResponse> getAttachmentsForTask(Integer taskId) {
        if(!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Không tìm thấy tác vụ");
        }
        
        List<TaskAttachment> attachments = attachmentRepository.findByTask_Id(taskId);
        return attachments.stream()
            .map(this::mapToAttachmentResponse)
            .collect(Collectors.toList());
    }
    
    // === Hàm Helper ===
    private TaskAttachmentResponse mapToAttachmentResponse(TaskAttachment a) {
        // Tạo một URL giả định để client có thể tải về
        // (Cần một API khác để xử lý việc tải file này)
        String fileUrl = String.format("/api/attachments/%d/download", a.getId());
        
        return TaskAttachmentResponse.builder()
            .id(a.getId())
            .taskId(a.getTask().getId())
            .fileName(a.getFileName())
            .fileType(a.getFileType())
            .fileSize(a.getFileSize())
            .fileUrl(fileUrl)
            .uploadedById(a.getUploadedBy().getId())
            .uploadedByName(a.getUploadedBy().getFullName())
            .uploadedAt(a.getUploadedAt())
            .build();
    }
}