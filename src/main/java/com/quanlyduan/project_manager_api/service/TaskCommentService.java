package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CommentRequest;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
import java.util.List;

public interface TaskCommentService {

    /**
     * Thêm một bình luận mới vào Task.
     * Logic nghiệp vụ sẽ xử lý việc tìm user, tìm task, và lưu bình luận.
     *
     * @param taskId ID của Task
     * @param request Nội dung bình luận và danh sách ID người được @mention
     * @return DTO của bình luận vừa tạo
     */
    TaskCommentResponse addComment(Integer taskId, CommentRequest request);

    /**
     * Lấy danh sách tất cả bình luận của một Task.
     * Đã được bảo vệ bởi @PreAuthorize ở Controller.
     *
     * @param taskId ID của Task
     * @return Danh sách DTO của các bình luận
     */
    List<TaskCommentResponse> getComments(Integer taskId);
}