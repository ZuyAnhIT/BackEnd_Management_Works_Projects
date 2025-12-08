// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/TaskAttachmentServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.TaskAttachment;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.TaskAttachmentRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.service.TaskAttachmentService;
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

    // Lấy đường dẫn thư mục upload từ cấu hình
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAttachmentRepository attachmentRepository;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public TaskAttachmentServiceImpl(TaskRepository taskRepository,
                                     UserRepository userRepository,
                                     TaskAttachmentRepository attachmentRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
    }

    // ======================================================
    // 1. LƯU ATTACHMENT (STORE FILE)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "UPLOAD", entityType = "TASK", description = "Upload file attactment")
    public TaskAttachmentResponse storeAttachment(Integer taskId, MultipartFile file, Integer uploaderId) throws IOException {

        // 1. Kiểm tra tồn tại Task và Uploader
        Task task = taskRepository.findById(taskId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));
        User uploader = userRepository.findById(uploaderId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Uploader user not found."));

        // 2. LOGIC LƯU FILE CỤC BỘ
        // Tạo thư mục con riêng cho từng Task (ví dụ: uploads/task-123)
        Path dirPath = Paths.get(uploadDir, "task-" + taskId);
        Files.createDirectories(dirPath); // Đảm bảo thư mục tồn tại

        // Tạo tên file duy nhất (timestamp + tên gốc)
        String originalFileName = file.getOriginalFilename();
        String storedFileName = System.currentTimeMillis() + "_" + originalFileName;
        Path filePath = dirPath.resolve(storedFileName);

        try (InputStream is = file.getInputStream()) {
            // Copy nội dung file vào đường dẫn vật lý
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 3. LƯU THÔNG TIN ATTACHMENT VÀO DATABASE
        TaskAttachment attachment = TaskAttachment.builder()
                .task(task)
                .fileName(originalFileName)
                .filePath(filePath.toString()) // Lưu đường dẫn vật lý đầy đủ
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedBy(uploader)
                .build();

        TaskAttachment saved = attachmentRepository.save(attachment);

        // 4. Trả về Response
        return mapToAttachmentResponse(saved);
    }

    // ======================================================
    // 2. LẤY DANH SÁCH ATTACHMENT (GET LIST)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<TaskAttachmentResponse> getAttachmentsForTask(Integer taskId) {
        // 1. Kiểm tra Task tồn tại
        if(!taskRepository.existsById(taskId)) {
            // Sửa thông báo sang tiếng Anh
            throw new ResourceNotFoundException("Task not found.");
        }

        // 2. Lấy danh sách Attachment từ DB
        List<TaskAttachment> attachments = attachmentRepository.findByTask_Id(taskId);

        // 3. Map sang DTO
        return attachments.stream()
                .map(this::mapToAttachmentResponse)
                .collect(Collectors.toList());
    }

    // ======================================================
    // ⚙️ PRIVATE HELPER: MAPPER
    // ======================================================
    /**
     * Helper: Map TaskAttachment Entity sang TaskAttachmentResponse DTO.
     */
    private TaskAttachmentResponse mapToAttachmentResponse(TaskAttachment a) {
        // Tạo một URL giả định để client có thể tải về
        // (Đây là đường dẫn API, không phải đường dẫn vật lý)
        String fileUrl = String.format("/api/attachments/%d/download", a.getId());

        return TaskAttachmentResponse.builder()
                .id(a.getId())
                .taskId(a.getTask().getId())
                .fileName(a.getFileName())
                .fileType(a.getFileType())
                .fileSize(a.getFileSize())
                .fileUrl(fileUrl) // Đường dẫn API để tải về
                .uploadedById(a.getUploadedBy().getId())
                .uploadedByName(a.getUploadedBy().getFullName())
                .uploadedAt(a.getUploadedAt())
                .build();
    }
}