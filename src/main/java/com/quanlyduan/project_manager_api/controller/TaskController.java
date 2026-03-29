package com.quanlyduan.project_manager_api.controller;

import java.io.IOException;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.dto.request.CommentRequest;
import com.quanlyduan.project_manager_api.dto.request.MoveTaskStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTaskSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.ImportTaskResultResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskAttachmentResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskImportPreviewResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.TaskAttachmentService;
import com.quanlyduan.project_manager_api.service.TaskCommentService;
import com.quanlyduan.project_manager_api.service.TaskService;

/**
 * Controller xử lý các nghiệp vụ liên quan đến công việc (Task), bao gồm chi tiết, bình luận, tệp đính kèm và nhập dữ liệu.
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin("*")
public class TaskController {

    // Khai báo các thông báo trả về (Response Messages)
    private static final String MSG_FETCH_DETAILS_SUCCESS = "Task details retrieved successfully.";
    private static final String MSG_UPDATE_SUCCESS = "Task updated successfully.";
    private static final String MSG_DELETE_SUCCESS = "Task deleted successfully.";
    private static final String MSG_EPIC_UPDATE_SUCCESS = "Task Epic updated successfully.";
    private static final String MSG_ARCHIVE_SUCCESS = "Task archived successfully.";
    private static final String MSG_RESTORE_SUCCESS = "Task restored successfully.";
    private static final String MSG_MOVE_SUCCESS = "Task moved successfully.";
    private static final String MSG_SPRINT_UPDATE_SUCCESS = "Task Sprint updated successfully.";
    private static final String MSG_BACKLOG_MOVE_SUCCESS = "Task moved to Backlog successfully.";
    private static final String MSG_COMMENT_ADD_SUCCESS = "Comment added successfully.";
    private static final String MSG_COMMENT_FETCH_SUCCESS = "Comments retrieved successfully.";
    private static final String MSG_UPLOAD_SUCCESS = "File uploaded successfully.";
    private static final String MSG_ATTACHMENT_FETCH_SUCCESS = "Task attachments retrieved successfully.";
    private static final String MSG_IMPORT_PREVIEW_SUCCESS = "Preview generated successfully.";
    private static final String MSG_IMPORT_SAVE_SUCCESS = "Tasks imported successfully.";

    private final TaskCommentService commentService;
    private final TaskAttachmentService attachmentService;
    private final SecurityService securityService;
    private final TaskService taskService;

    // Khởi tạo thủ công để tiêm phụ thuộc (Dependency Injection)
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
    // QUẢN LÝ THÔNG TIN CHÍNH (CRUD & TRẠNG THÁI ĐẶC BIỆT)
    // ======================================================

    /**
     * Lấy thông tin chi tiết của một công việc.
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:view')")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskDetails(@PathVariable Integer taskId) {
        TaskResponse task = taskService.getTaskDetails(taskId);
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_DETAILS_SUCCESS, task));
    }

    /**
     * Cập nhật toàn bộ thông tin của công việc.
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:edit')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Integer taskId,
            @Valid @RequestBody UpdateTaskRequest request) {

        TaskResponse updatedTask = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_SUCCESS, updatedTask));
    }

    /**
     * Xóa bỏ hoàn toàn công việc khỏi hệ thống.
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("@securityService.hasPermission('task', #taskId, 'task:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteTask(@PathVariable Integer taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(MSG_DELETE_SUCCESS, null));
    }

    /**
     * Thay đổi Epic gắn liền với công việc.
     */
    @PatchMapping("/{taskId}/epic")
    @PreAuthorize("@securityService.hasPermission('task', #taskId, 'project:edit')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskEpic(
            @PathVariable Integer taskId,
            @Valid @RequestBody UpdateTaskEpicRequest request) {

        TaskResponse task = taskService.updateTaskEpic(taskId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_EPIC_UPDATE_SUCCESS, task));
    }

    /**
     * Đưa công việc vào kho lưu trữ (Archive).
     */
    @PatchMapping("/{taskId}/archive")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> archiveTask(@PathVariable Integer taskId) {
        taskService.archiveTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(MSG_ARCHIVE_SUCCESS, null));
    }

    /**
     * Khôi phục công việc từ kho lưu trữ về trạng thái bình thường.
     */
    @PatchMapping("/{taskId}/restore")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> restoreTask(@PathVariable Integer taskId) {
        taskService.restoreTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(MSG_RESTORE_SUCCESS, null));
    }

    // ======================================================
    // QUẢN LÝ VỊ TRÍ & CHU KỲ (MOVE & SPRINT)
    // ======================================================

    /**
     * Di chuyển công việc sang cột trạng thái khác hoặc thay đổi vị trí trong cột.
     */
    @PutMapping("/{taskId}/move")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> moveTask(
            @PathVariable Integer taskId,
            @Valid @RequestBody MoveTaskStatusRequest request) {

        taskService.moveTaskToStatus(taskId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_MOVE_SUCCESS, null));
    }

    /**
     * Chuyển công việc vào một Sprint cụ thể hoặc đưa về danh sách Backlog.
     */
    @PutMapping("/{taskId}/sprint")
    @PreAuthorize("@securityService.hasPermission('task', #taskId, 'backlog:manage')")
    public ResponseEntity<ApiResponse<Object>> updateTaskSprint(
            @PathVariable Integer taskId,
            @Valid @RequestBody UpdateTaskSprintRequest request) {

        taskService.updateTaskSprint(taskId, request.getSprintId(), request.getNewSortOrder());
        String message = (request.getSprintId() == null) ? MSG_BACKLOG_MOVE_SUCCESS : MSG_SPRINT_UPDATE_SUCCESS;
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // ======================================================
    // TƯƠNG TÁC & THẢO LUẬN (COMMENTS)
    // ======================================================

    /**
     * Thêm bình luận mới vào công việc.
     */
    @PostMapping("/{taskId}/comments")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:comment')")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable Integer taskId,
            @Valid @RequestBody CommentRequest request) {

        TaskCommentResponse newComment = commentService.addComment(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MSG_COMMENT_ADD_SUCCESS, newComment));
    }

    /**
     * Lấy danh sách toàn bộ bình luận của một công việc.
     */
    @GetMapping("/{taskId}/comments")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:comment:view')")
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getComments(@PathVariable Integer taskId) {
        List<TaskCommentResponse> comments = commentService.getComments(taskId);
        return ResponseEntity.ok(ApiResponse.success(MSG_COMMENT_FETCH_SUCCESS, comments));
    }

    // ======================================================
    // QUẢN LÝ TỆP ĐÍNH KÈM (ATTACHMENTS)
    // ======================================================

    /**
     * Tải tệp tin lên và đính kèm vào công việc.
     */
    @PostMapping(value = "/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:attach_file')")
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> uploadAttachment(
            @PathVariable Integer taskId,
            @RequestParam("file") MultipartFile file) throws IOException {

        Integer uploaderId = securityService.getCurrentUserId();
        TaskAttachmentResponse attachment = attachmentService.storeAttachment(taskId, file, uploaderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MSG_UPLOAD_SUCCESS, attachment));
    }

    /**
     * Lấy danh sách các tệp tin đã đính kèm vào công việc.
     */
    @GetMapping("/{taskId}/attachments")
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:view')")
    public ResponseEntity<ApiResponse<List<TaskAttachmentResponse>>> getAttachments(@PathVariable Integer taskId) {
        List<TaskAttachmentResponse> attachments = attachmentService.getAttachmentsForTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(MSG_ATTACHMENT_FETCH_SUCCESS, attachments));
    }

    // ======================================================
    // TIỆN ÍCH NHẬP DỮ LIỆU HÀNG LOẠT (IMPORT)
    // ======================================================

    /**
     * Tải xuống file mẫu Excel để phục vụ việc nhập dữ liệu công việc hàng loạt.
     */
    @GetMapping("/import-template")
    public ResponseEntity<Resource> downloadImportTemplate() {
        String filename = "tasks_import_template.xlsx";
        byte[] excelContent = taskService.generateImportTemplate();
        ByteArrayResource resource = new ByteArrayResource(excelContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    /**
     * Phân tích và hiển thị kết quả xem trước từ tệp tin Excel được tải lên.
     */
    @PostMapping(value = "/{projectId}/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:create')")
    public ResponseEntity<ApiResponse<List<TaskImportPreviewResponse>>> previewImport(
            @PathVariable Integer projectId,
            @RequestParam("file") MultipartFile file) {
        List<TaskImportPreviewResponse> preview = taskService.previewImportTasks(projectId, file);
        return ResponseEntity.ok(ApiResponse.success(MSG_IMPORT_PREVIEW_SUCCESS, preview));
    }

    /**
     * Lưu trữ dữ liệu công việc từ kết quả xem trước vào cơ sở dữ liệu.
     */
    @PostMapping("/{projectId}/import/save")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:create')")
    public ResponseEntity<ApiResponse<ImportTaskResultResponse>> saveImport(
            @PathVariable Integer projectId,
            @RequestBody List<TaskImportPreviewResponse> rows) {
        ImportTaskResultResponse result = taskService.saveImportedTasks(projectId, rows);
        return ResponseEntity.ok(ApiResponse.success(MSG_IMPORT_SAVE_SUCCESS, result));
    }
}