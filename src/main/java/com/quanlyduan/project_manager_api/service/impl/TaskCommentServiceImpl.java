// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/TaskCommentServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.CommentRequest;
import com.quanlyduan.project_manager_api.dto.response.TaskCommentResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.TaskComment;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.TaskCommentRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.TaskCommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public TaskCommentServiceImpl(TaskCommentRepository commentRepository,
                                  TaskRepository taskRepository,
                                  UserRepository userRepository,
                                  SecurityService securityService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
    }

    // ======================================================
    // 1. THÊM BÌNH LUẬN (ADD COMMENT)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "COMMENT", entityType = "TASK", description = "Comment on task")
    public TaskCommentResponse addComment(Integer taskId, CommentRequest request) {

        // 1. Lấy user hiện tại (người bình luận)
        Integer currentUserId = securityService.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));

        // 2. Tìm Task
        Task task = taskRepository.findById(taskId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        // 3. XỬ LÝ: Logic @mentions đã bị loại bỏ như trong code gốc
        /*
        Set<User> mentionedUsers = new HashSet<>();
        if (request.getMentionedUserIds() != null && !request.getMentionedUserIds().isEmpty()) {
            List<User> foundUsers = userRepository.findAllById(request.getMentionedUserIds());
            mentionedUsers.addAll(foundUsers);
        }
        */

        // 4. Tạo và lưu bình luận
        TaskComment newComment = TaskComment.builder()
                .content(request.getContent())
                .task(task)
                .user(currentUser)
                // .mentionedUsers(mentionedUsers) // Bỏ qua logic mentioned users
                .build();

        TaskComment savedComment = commentRepository.save(newComment);

        // 5. Map sang DTO và trả về
        return mapToCommentResponse(savedComment);
    }

    // ======================================================
    // 2. LẤY DANH SÁCH BÌNH LUẬN (GET COMMENTS)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<TaskCommentResponse> getComments(Integer taskId) {

        // 1. Kiểm tra Task tồn tại
        if (!taskRepository.existsById(taskId)) {
            // Sửa thông báo sang tiếng Anh
            throw new ResourceNotFoundException("Task not found with ID: " + taskId);
        }

        // 2. Lấy danh sách bình luận (sắp xếp theo thời gian tạo ASC)
        List<TaskComment> comments = commentRepository.findByTask_IdOrderByCreatedAtAsc(taskId);

        // 3. Map và trả về
        return comments.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    // ======================================================
    // ⚙️ PRIVATE HELPER: MAPPER
    // ======================================================

    /**
     * Helper: Map TaskComment Entity sang TaskCommentResponse DTO.
     */
    private TaskCommentResponse mapToCommentResponse(TaskComment comment) {

        // 1. Map thông tin người bình luận
        TaskCommentResponse.CommentUserResponse commentUser = TaskCommentResponse.CommentUserResponse.builder()
                .userId(comment.getUser().getId())
                .fullName(comment.getUser().getFullName())
                .avatarUrl(comment.getUser().getAvatarUrl())
                .build();

        // 2. XÓA BỎ LOGIC MAP DANH SÁCH MENTION
        /*
        List<TaskCommentResponse.CommentUserResponse> mentionedUsersList = comment.getMentionedUsers().stream()
                    .map(user -> TaskCommentResponse.CommentUserResponse.builder()
                            .userId(user.getId())
                            .fullName(user.getFullName())
                            .avatarUrl(user.getAvatarUrl())
                            .build())
                    .collect(Collectors.toList());
        */

        // 3. Xây dựng DTO Response cuối cùng
        return TaskCommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .user(commentUser) // Thông tin người viết
                // .mentionedUsers(mentionedUsersList) // Bỏ qua logic mentioned users
                .build();
    }
}