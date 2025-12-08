// File: src/main/java/com/quanlyduan/project_manager_api/controller/TaskController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CommentRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.service.TaskAttachmentService;
import com.quanlyduan.project_manager_api.service.TaskCommentService;
import com.quanlyduan.project_manager_api.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import com.quanlyduan.project_manager_api.security.SecurityService;
import java.util.List;

@RestController
@RequestMapping("/api/tasks") // Tất cả API liên quan đến Task sẽ bắt đầu bằng /api/tasks
@CrossOrigin("*")
/**
 * Controller xử lý các nghiệp vụ liên quan đến Task (chi tiết Task, comment, attachment).
 */
public class TaskController {

    private final TaskCommentService commentService;
    private final TaskAttachmentService attachmentService;
    private final SecurityService securityService;
    private final TaskService taskService;

    // CONSTRUCTOR THỦ CÔNG
    public TaskController(TaskCommentService commentService,
                          TaskAttachmentService attachmentService,
                          SecurityService securityService,
                          TaskService taskService) {
        this.commentService = commentService;
        this.attachmentService = attachmentService;
        this.securityService = securityService;
        this.taskService = taskService;
    }

    // ======================================================
    // A. QUẢN LÝ THÔNG TIN CHÍNH (CRUD)
    // ======================================================

    // API XEM CHI TIẾT TASK
    @GetMapping("/{taskId}")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:view')")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskDetails(
            @PathVariable Integer taskId) {

        TaskResponse task = taskService.getTaskDetails(taskId);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Task details retrieved successfully.", task));
    }


    // API CẬP NHẬT THÔNG TIN TASK (PUT/FULL UPDATE)
    @PutMapping("/{taskId}")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:edit')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Integer taskId,
            @Valid @RequestBody UpdateTaskRequest request) {

        TaskResponse updatedTask = taskService.updateTask(taskId, request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully.", updatedTask));
    }
    @DeleteMapping("/{taskId}")
    @PreAuthorize("@securityService.hasPermission('task', #taskId, 'task:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteTask(@PathVariable Integer taskId) {
        
        taskService.deleteTask(taskId);
        
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully.", null));
    }
    // API GÁN/GỠ EPIC CHO TASK
    @PatchMapping("/{taskId}/epic")
    // Bảo vệ: Task phải thuộc về Project mà User có quyền sửa (project:edit)
    @PreAuthorize("@securityService.hasPermission('task', #taskId, 'project:edit')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskEpic(
            @PathVariable Integer taskId,
            @Valid @RequestBody UpdateTaskEpicRequest request) {

        TaskResponse task = taskService.updateTaskEpic(taskId, request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Task Epic updated successfully.", task));
    }

    // ======================================================
    // B. KÉO THẢ & TRẠNG THÁI
    // ======================================================

    // KÉO THẢ TASK SANG CỘT KHÁC (Thay đổi trạng thái và vị trí)
    @PutMapping("/{taskId}/move")
    // Bảo vệ: Cần quyền 'task:edit' (Sửa task)
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> moveTask(
            @PathVariable Integer taskId,
            @Valid @RequestBody MoveTaskStatusRequest request) {

        taskService.moveTaskToStatus(taskId, request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Task moved successfully.", null));
    }

    // KÉO THẢ TASK VÀO SPRINT (hoặc về Backlog)
    @PutMapping("/{taskId}/sprint")
    @PreAuthorize("@securityService.hasPermission('task', #taskId, 'backlog:manage')")
    public ResponseEntity<ApiResponse<Object>> updateTaskSprint(
            @PathVariable Integer taskId,
            @Valid @RequestBody UpdateTaskSprintRequest request) {


        taskService.updateTaskSprint(taskId, request.getSprintId(), request.getNewSortOrder());

        // Tạo thông báo động
        String message = (request.getSprintId() == null) ? "Task moved to Backlog successfully." : "Task Sprint updated successfully.";
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }


    // ======================================================
    // C. BÌNH LUẬN (COMMENTS)
    // ======================================================

    // API Thêm bình luận vào Task
    @PostMapping("/{taskId}/comments")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:comment')")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable Integer taskId,
            @Valid @RequestBody CommentRequest request) {

        // 1. Gọi service để thực hiện logic
        TaskCommentResponse newComment = commentService.addComment(taskId, request);

        // 2. Đóng gói kết quả vào ApiResponse
        ApiResponse<TaskCommentResponse> response = ApiResponse.success(
                // Sửa thông báo trả về sang tiếng Anh
                "Comment added successfully.",
                newComment
        );

        // 3. Trả về 201 CREATED vì đã tạo mới thành công
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // API Lấy danh sách bình luận của Task
    @GetMapping("/{taskId}/comments")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:comment:view')")
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getComments(
            @PathVariable Integer taskId) {

        // 1. Gọi service để lấy dữ liệu
        List<TaskCommentResponse> comments = commentService.getComments(taskId);

        // 2. Đóng gói kết quả
        ApiResponse<List<TaskCommentResponse>> response = ApiResponse.success(
                // Sửa thông báo trả về sang tiếng Anh
                "Comments retrieved successfully.",
                comments
        );

        // 3. Trả về 200 OK
        return ResponseEntity.ok(response);
    }

    // ======================================================
    // D. TỆP ĐÍNH KÈM (ATTACHMENTS)
    // ======================================================

    // Đính kèm tệp tin
    @PostMapping(value = "/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:attach_file')")
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> uploadAttachment(
            @PathVariable Integer taskId,
            @RequestParam("file") MultipartFile file) throws IOException {

        Integer uploaderId = securityService.getCurrentUserId();
        TaskAttachmentResponse attachment = attachmentService.storeAttachment(taskId, file, uploaderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                // Sửa thông báo trả về sang tiếng Anh
                .body(ApiResponse.success("File uploaded successfully.", attachment));
    }

    // Lấy danh sách tệp tin
    @GetMapping("/{taskId}/attachments")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:view')") // Chỉ cần quyền xem Task
    public ResponseEntity<ApiResponse<List<TaskAttachmentResponse>>> getAttachments(
            @PathVariable Integer taskId) {

        List<TaskAttachmentResponse> attachments = attachmentService.getAttachmentsForTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task attachments retrieved successfully.", attachments));
    }
}