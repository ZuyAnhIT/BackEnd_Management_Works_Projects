package com.quanlyduan.project_manager_api.service;

// Import 3 file bạn đã cung cấp
import com.quanlyduan.project_manager_api.repository.TaskAttachmentRepository; // File 1
import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse; // File 2
import com.quanlyduan.project_manager_api.model.TaskAttachment; // File 4

// Các import cần thiết khác (bạn cần tự thêm vào)
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
// import com.quanlyduan.project_manager_api.service.IStorageService; // Cần 1 service để lưu file
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskAttachmentServiceImpl implements TaskAttachmentService {

    // Tiêm (Inject) File 1 (Repository)
    @Autowired
    private TaskAttachmentRepository attachmentRepository;

    // (Giả sử bạn cũng đã có các Repository này)
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    // (Giả sử bạn có một Service riêng để xử lý lưu file vật lý, ví dụ: lưu lên S3 hoặc Server)
    // @Autowired
    // private IStorageService storageService;

    /**
     * ỨNG DỤNG File 1, 2, 4
     */
    @Override
    public List<TaskAttachmentResponse> getAttachmentsForTask(Integer taskId) {
        // 1. Dùng File 1 (Repository) để lấy danh sách File 4 (Entity)
        List<TaskAttachment> entities = attachmentRepository.findByTask_IdOrderByUploadedAtAsc(taskId);

        // 2. Chuyển đổi (map) danh sách File 4 (Entity) sang File 2 (DTO)
        return entities.stream()
                .map(this::mapToAttachmentResponse) // Gọi hàm trợ giúp bên dưới
                .collect(Collectors.toList());
    }

    @Override
    public TaskAttachmentResponse storeAttachment(Integer taskId, MultipartFile file, Integer uploaderId) throws IOException {
        // (Đây là logic giả định, bạn cần 1 StorageService để lưu file)
        
        // 1. Tìm User và Task từ ID
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // 2. Gọi StorageService để lưu file và lấy URL
        // String fileUrl = storageService.storeFile(file); // Ví dụ
        String fileUrl = "/uploads/" + file.getOriginalFilename(); // (Ví dụ đơn giản)

        // 3. Tạo File 4 (Entity)
        TaskAttachment attachment = TaskAttachment.builder()
                .fileName(file.getOriginalFilename())
                .fileUrl(fileUrl)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .user(uploader)
                .task(task)
                .build();

        // 4. Dùng File 1 (Repository) để lưu File 4 (Entity)
        TaskAttachment savedAttachment = attachmentRepository.save(attachment);

        // 5. Chuyển đổi sang File 2 (DTO) để trả về
        return mapToAttachmentResponse(savedAttachment);
    }

    @Override
    public void deleteAttachment(Integer attachmentId, Integer uploaderId) {
        // (Thêm logic kiểm tra quyền: chỉ người upload hoặc PM mới được xóa)
        TaskAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        
        // (Thêm logic gọi StorageService để xóa file vật lý)
        // storageService.deleteFile(attachment.getFileUrl());

        // Xóa trong CSDL
        attachmentRepository.delete(attachment);
    }


    /**
     * HÀM "ĐỒNG BỘ" (ÁNH XẠ) - NƠI ỨNG DỤNG CẢ 3 FILE
     * Hàm này nhận vào File 4 (Entity) và trả về File 2 (DTO)
     */
    private TaskAttachmentResponse mapToAttachmentResponse(TaskAttachment entity) {
        if (entity == null) {
            return null;
        }

        // Lấy thông tin người upload từ Entity (File 4)
        User uploaderEntity = entity.getUser();

        // Tạo DTO con (UploaderInfo)
        TaskCommentResponse.CommentUserResponse uploaderDto = TaskCommentResponse.CommentUserResponse.builder()
                .userId(uploaderEntity.getId())
                .fullName(uploaderEntity.getFullName()) // (Giả sử User Entity có trường này)
                .avatarUrl(uploaderEntity.getAvatarUrl()) // (Giả sử User Entity có trường này)
                .build();

        // Dùng builder của File 2 (DTO) để tạo đối tượng response
        return TaskAttachmentResponse.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .fileUrl(entity.getFileUrl())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .uploadedAt(entity.getUploadedAt())
                .uploader(uploaderDto) // Gán DTO con vào
                .build();
    }
}