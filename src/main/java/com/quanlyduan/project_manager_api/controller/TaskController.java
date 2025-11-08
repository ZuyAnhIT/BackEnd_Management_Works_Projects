package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CommentRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
import com.quanlyduan.project_manager_api.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks") // Tất cả API liên quan đến Task sẽ bắt đầu bằng /api/tasks
@RequiredArgsConstructor
public class TaskController {

    private final TaskCommentService commentService;

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
                "Comment added successfully",
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
                "Fetched comments successfully",
                comments
        );

        // 3. Trả về 200 OK
        return ResponseEntity.ok(response);
    }
}