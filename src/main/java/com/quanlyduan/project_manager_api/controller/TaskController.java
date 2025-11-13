package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CommentRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
import com.quanlyduan.project_manager_api.service.TaskAttachmentService;
import com.quanlyduan.project_manager_api.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.springframework.http.MediaType;
import com.quanlyduan.project_manager_api.security.SecurityServicePermission;
import java.util.List;

@RestController
@RequestMapping("/api/tasks") // Tất cả API liên quan đến Task sẽ bắt đầu bằng /api/tasks
@RequiredArgsConstructor
public class TaskController {

    private final TaskCommentService commentService;
    private final TaskAttachmentService attachmentService;
    private final SecurityServicePermission securityServicePermission;
    


    // TODO: Thêm các API khác liên quan đến Task (ví dụ: Create Task, Get Task Details...)

    /**
     * API Thêm bình luận vào Task
     * Endpoint: POST /api/tasks/{taskId}/comments
     */
    @PostMapping("/{taskId}/comments")
    @PreAuthorize("@securityServicePermission.hasTaskPermission(#taskId, 'task:comment')")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable Integer taskId,
            @Valid @RequestBody CommentRequest request) {

        // 1. Gọi service để thực hiện logic
        TaskCommentResponse newComment = commentService.addComment(taskId, request);

        // 2. Đóng gói kết quả vào ApiResponse (theo chuẩn của base code)
        ApiResponse<TaskCommentResponse> response = ApiResponse.success(
                "Thêm bình luận thành công.",
                newComment
        );

        // 3. Trả về 201 CREATED vì đã tạo mới thành công
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

        // 1. Gọi service để lấy dữ liệu
        List<TaskCommentResponse> comments = commentService.getComments(taskId);

        // 2. Đóng gói kết quả
        ApiResponse<List<TaskCommentResponse>> response = ApiResponse.success(
                "Lấy danh sách bình luận thành công.",
                comments
        );

        // 3. Trả về 200 OK
        return ResponseEntity.ok(response);
    }

      /**
     * User Story 11: Đính kèm tệp tin
     */
    @PostMapping(value = "/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityServicePermission.hasTaskPermission(#taskId, 'task:attach_file')")
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> uploadAttachment(
            @PathVariable Integer taskId,
            @RequestParam("file") MultipartFile file) throws IOException { // <-- Dòng này giữ nguyên
        
        Integer uploaderId = securityServicePermission.getCurrentUserId();
        TaskAttachmentResponse attachment = attachmentService.storeAttachment(taskId, file, uploaderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải tệp lên thành công.", attachment));
    }

    /**
     * User Story 11: Lấy danh sách tệp tin (Guest cũng xem được)
     */
    @GetMapping("/{taskId}/attachments")
    @PreAuthorize("@securityServicePermission.hasTaskPermission(#taskId, 'task:view')") // Chỉ cần quyền xem Task
    public ResponseEntity<ApiResponse<List<TaskAttachmentResponse>>> getAttachments(
            @PathVariable Integer taskId) {
        
        List<TaskAttachmentResponse> attachments = attachmentService.getAttachmentsForTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách của tệp đính kèm thành công.", attachments));
    }


}