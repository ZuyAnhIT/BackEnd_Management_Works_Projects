// File: src/main/java/com/quanlyduan/project_manager_api/controller/TaskController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CommentRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.ImportResultResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskImportPreviewResponse;
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
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.quanlyduan.project_manager_api.security.SecurityService;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

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

    // API LƯU TRỮ TASK
    @PatchMapping("/{taskId}/archive")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> archiveTask(@PathVariable Integer taskId) {
        taskService.archiveTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task archived successfully.", null));
    }

    // API KHÔI PHỤC TASK
    @PatchMapping("/{taskId}/restore")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> restoreTask(@PathVariable Integer taskId) {
        taskService.restoreTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task restored successfully.", null));
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

    // API 1: Tải file mẫu CSV
    @GetMapping("/tasks/import-template")
    public ResponseEntity<Resource> downloadImportTemplate() {
        String filename = "tasks_import_template.csv";
        // Nội dung mẫu: Header + 1 dòng ví dụ
        String content = "Title,Description,Assignee Email,Priority,Status,Due Date,Story Points,Estimated Hours\n" +
                         "Fix Login Bug,Fix error 401 on login,dev1@techvision.com,HIGH,To Do,2025-12-31,5,8.0";
        
        ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    // API 2: Import Task
    @PostMapping(value = "/{projectId}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:create')")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importTasks(
            @PathVariable Integer projectId,
            @RequestParam("file") MultipartFile file) { // Chỉ cần 2 tham số này là đủ

        // Gọi Service (Service sẽ tự tìm companyId, workspaceId)
        ImportResultResponse result = taskService.importTasksFromCsv(projectId, file);

        if (result.getErrorCount() > 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Import failed with errors", result));
        }
        return ResponseEntity.ok(ApiResponse.success("Tasks imported successfully", result));
    }
    // API 1: Preview (Upload file -> Trả về JSON để review)
    @PostMapping(value = "/{projectId}/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:create')")
    public ResponseEntity<ApiResponse<List<TaskImportPreviewResponse>>> previewImport(
            @PathVariable Integer projectId,
            @RequestParam("file") MultipartFile file) {
        List<TaskImportPreviewResponse> preview = taskService.previewImportTasks(projectId, file);
        return ResponseEntity.ok(ApiResponse.success("Preview generated", preview));
    }

    // API 2: Save (Gửi JSON đã sửa -> Lưu DB)
    @PostMapping("/{projectId}/import/save")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:create')")
    public ResponseEntity<ApiResponse<ImportResultResponse>> saveImport(
            @PathVariable Integer projectId,
            @RequestBody List<TaskImportPreviewResponse> rows) {
        ImportResultResponse result = taskService.saveImportedTasks(projectId, rows);
        return ResponseEntity.ok(ApiResponse.success("Tasks imported successfully", result));
    }
}