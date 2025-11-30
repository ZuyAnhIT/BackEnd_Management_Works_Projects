// File: src/main/java/com/quanlyduan/project_manager_api/service/TaskCommentService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CommentRequest;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
import java.util.List;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến Bình luận (Comment) trong Task.
 */
public interface TaskCommentService {

    // ========================================================================
    // 1. GHI VÀ LƯU BÌNH LUẬN (WRITE)
    // ========================================================================

    /**
     * Thêm một bình luận mới vào Task.
     * Logic nghiệp vụ sẽ xử lý việc tìm user, tìm task, và lưu bình luận.
     *
     * @param taskId ID của Task
     * @param request Nội dung bình luận
     * @return DTO của bình luận vừa tạo
     */
    TaskCommentResponse addComment(Integer taskId, CommentRequest request);

    // ========================================================================
    // 2. LẤY DANH SÁCH (READ)
    // ========================================================================

    /**
     * Lấy danh sách tất cả bình luận của một Task.
     * Dữ liệu trả về đã được sắp xếp theo thời gian tạo (cũ nhất lên trước).
     *
     * @param taskId ID của Task
     * @return Danh sách DTO của các bình luận
     */
    List<TaskCommentResponse> getComments(Integer taskId);
}