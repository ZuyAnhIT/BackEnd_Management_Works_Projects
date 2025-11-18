// File: src/main/java/com/quanlyduan/project_manager_api/controller/TaskController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CommentRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
// import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest; // Đã xóa
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
// import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse; // Đã xóa
import com.quanlyduan.project_manager_api.service.TaskAttachmentService;
import com.quanlyduan.project_manager_api.service.TaskCommentService;
import com.quanlyduan.project_manager_api.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.springframework.http.MediaType;
import com.quanlyduan.project_manager_api.security.SecurityService; 
import java.util.List;

@RestController
@RequestMapping("/api/tasks") // Tất cả API liên quan đến Task sẽ bắt đầu bằng /api/tasks
@CrossOrigin("*")
// @RequiredArgsConstructor // Đã xóa
public class TaskController {

    private final TaskCommentService commentService;
    private final TaskAttachmentService attachmentService;
    private final SecurityService securityService; // Đã sửa
    private final TaskService taskService;

    // *** THÊM CONSTRUCTOR THỦ CÔNG ***
    public TaskController(TaskCommentService commentService,
                          TaskAttachmentService attachmentService,
                          SecurityService securityService,
                          TaskService taskService) {
        this.commentService = commentService;
        this.attachmentService = attachmentService;
        this.securityService = securityService;
        this.taskService = taskService;
    }
    
    /**
     * API Thêm bình luận vào Task
     * Endpoint: POST /api/tasks/{taskId}/comments
     */
    @PostMapping("/{taskId}/comments")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:comment')") // Đã sửa
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
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:comment:view')") // Đã sửa
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
     * Sp2 - User Story 11: Đính kèm tệp tin
     */
    @PostMapping(value = "/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:attach_file')") // Đã sửa
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> uploadAttachment(
            @PathVariable Integer taskId,
            @RequestParam("file") MultipartFile file) throws IOException { // <-- Dòng này giữ nguyên
        
        Integer uploaderId = securityService.getCurrentUserId(); // Đã sửa
        TaskAttachmentResponse attachment = attachmentService.storeAttachment(taskId, file, uploaderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải tệp lên thành công.", attachment));
    }

    /**
     * Sp2 - User Story 11: Lấy danh sách tệp tin (Guest cũng xem được)
     */
    @GetMapping("/{taskId}/attachments")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:view')") // Chỉ cần quyền xem Task // Đã sửa
    public ResponseEntity<ApiResponse<List<TaskAttachmentResponse>>> getAttachments(
            @PathVariable Integer taskId) {
        
        List<TaskAttachmentResponse> attachments = attachmentService.getAttachmentsForTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tệp đính kèm thành công.", attachments));
    }

    /**
     * US-S3-7: Kéo thả Task vào Sprint (hoặc về Backlog)
     */
    @PutMapping("/{taskId}/sprint")
    @PreAuthorize("@securityService.hasPermission('task', #taskId, 'backlog:manage')") // Đã sửa
    public ResponseEntity<ApiResponse<Object>> updateTaskSprint(
            @PathVariable Integer taskId,
            @Valid @RequestBody UpdateTaskSprintRequest request) {
        
        taskService.updateTaskSprint(taskId, request.getSprintId());
        String message = (request.getSprintId() == null) ? "Chuyển công việc về Backlog thành công" : "Cập nhật Sprint cho công việc thành công"; // Đã dịch
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    /**
     * US: Kéo thả Task sang cột khác (Thay đổi trạng thái)
     * Endpoint: PUT /api/tasks/{taskId}/move
     */
    @PutMapping("/{taskId}/move")
    // Bảo vệ: Cần quyền 'task:edit' (Sửa task)
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> moveTask(
            @PathVariable Integer taskId,
            @Valid @RequestBody MoveTaskStatusRequest request) {
        
        taskService.moveTaskToStatus(taskId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Di chuyển công việc thành công.", null)); // Đã dịch
    }
}