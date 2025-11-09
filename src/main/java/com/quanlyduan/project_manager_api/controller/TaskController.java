package com.quanlyduan.project_manager_api.controller;

// --- Import cho DTOs ---
import com.quanlyduan.project_manager_api.dto.request.CommentRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse; // BỔ SUNG (File 3)

// --- Import cho Services ---
import com.quanlyduan.project_manager_api.service.TaskCommentService;
import com.quanlyduan.project_manager_api.service.TaskAttachmentService; // BỔ SUNG (File 5)
import com.quanlyduan.project_manager_api.service.SecurityService; // BỔ SUNG (File 4)

// --- Import cho Validation ---
import jakarta.validation.Valid;

// --- Import cho Lombok ---
import lombok.RequiredArgsConstructor;

// --- Import cho Spring Framework ---
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // BỔ SUNG

// --- Import cho Java Utils ---
import java.io.IOException; // BỔ SUNG
import java.util.List;

@RestController
@RequestMapping("/api/tasks") // Tất cả API liên quan đến Task sẽ bắt đầu bằng /api/tasks
@RequiredArgsConstructor
public class TaskController {

    // --- Services đã được tiêm (Inject) ---
    private final TaskCommentService commentService;
    private final TaskAttachmentService attachmentService; // BỔ SUNG (File 5)
    private final SecurityService securityService;         // BỔ SUNG (File 4)

    // =================================================================
    // API CHO BÌNH LUẬN (COMMENT) - (Phần đã có)
    // =================================================================

    /**
     * API Thêm bình luận vào Task
     * Endpoint: POST /api/tasks/{taskId}/comments
     */
    @PostMapping("/{taskId}/comments")
    @PreAuthorize("@securityServicePermission.hasTaskPermission(#taskId, 'task:comment')")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable Integer taskId,
            @Valid @RequestBody CommentRequest request) {

        TaskCommentResponse newComment = commentService.addComment(taskId, request);
        ApiResponse<TaskCommentResponse> response = ApiResponse.success(
                "Comment added successfully",
                newComment
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API Lấy danh sách bình luận của Task
     * Endpoint: GET /api/tasks/{taskId}/comments
     */
    @GetMapping("/{taskId}/comments")
    @PreAuthorize("@securityServicePermission.hasTaskPermission(#taskId, 'task:comment:view')")
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getComments(
            @PathVariable Integer taskId) {

        List<TaskCommentResponse> comments = commentService.getComments(taskId);
        ApiResponse<List<TaskCommentResponse>> response = ApiResponse.success(
                "Fetched comments successfully",
                comments
        );
        return ResponseEntity.ok(response);
    }

    // =================================================================
    // API CHO TỆP TIN (ATTACHMENT) - (PHẦN MỚI BỔ SUNG)
    // =================================================================

    /**
     * API Tải tệp tin (Upload) đính kèm cho Task
     * Endpoint: POST /api/tasks/{taskId}/attachments
     */
    @PostMapping("/{taskId}/attachments")
    @PreAuthorize("@securityServicePermission.hasTaskPermission(#taskId, 'task:attach_file')")
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> uploadAttachment(
            @PathVariable Integer taskId,
            @RequestParam("file") MultipartFile file) throws IOException {

        // 1. Lấy ID người dùng từ SecurityService (File 4)
        Integer uploaderId = securityService.getCurrentAuthenticatedUser().getId();

        // 2. Gọi service (File 5) để lưu file (sử dụng File 1, 2, 6)
        TaskAttachmentResponse attachment = attachmentService.storeAttachment(taskId, file, uploaderId);

        // 3. Đóng gói kết quả (File 3)
        ApiResponse<TaskAttachmentResponse> response = ApiResponse.success(
                "File uploaded successfully",
                attachment
        );

        // 4. Trả về 201 CREATED
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API Lấy danh sách tệp tin của Task
     * Endpoint: GET /api/tasks/{taskId}/attachments
     */
    @GetMapping("/{taskId}/attachments")
    @PreAuthorize("@securityServicePermission.hasTaskPermission(#taskId, 'task:view')") // Giả định quyền xem
    public ResponseEntity<ApiResponse<List<TaskAttachmentResponse>>> getAttachments(
            @PathVariable Integer taskId) {

        // 1. Gọi service (File 5) để lấy danh sách
        List<TaskAttachmentResponse> attachments = attachmentService.getAttachmentsForTask(taskId);

        // 2. Đóng gói kết quả
        ApiResponse<List<TaskAttachmentResponse>> response = ApiResponse.success(
                "Fetched attachments successfully",
                attachments
        );

        // 3. Trả về 200 OK
        return ResponseEntity.ok(response);
    }
}