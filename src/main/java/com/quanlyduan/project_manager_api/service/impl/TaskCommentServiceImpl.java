// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/TaskCommentServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

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
    private final SecurityService securityService; // Đã sửa

    // *** THÊM CONSTRUCTOR THỦ CÔNG ***
    public TaskCommentServiceImpl(TaskCommentRepository commentRepository, 
                                  TaskRepository taskRepository, 
                                  UserRepository userRepository, 
                                  SecurityService securityService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
    }

    @Override
    @Transactional
    public TaskCommentResponse addComment(Integer taskId, CommentRequest request) {

        // 1. Lấy user hiện tại (người bình luận)
        Integer currentUserId = securityService.getCurrentUserId(); // Đã sửa
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại"));

        // 2. Tìm Task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác vụ với ID: " + taskId));

        // 3. SỬA: XÓA BỎ XỬ LÝ @mentions
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
                // .mentionedUsers(mentionedUsers) // SỬA: Xóa
                .build();

        TaskComment savedComment = commentRepository.save(newComment);

        // 5. Map sang DTO và trả về
        return mapToCommentResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskCommentResponse> getComments(Integer taskId) {

        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Không tìm thấy tác vụ với ID: " + taskId);
        }

        List<TaskComment> comments = commentRepository.findByTask_IdOrderByCreatedAtAsc(taskId);

        return comments.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    // --- Private Helper Methods ---

    private TaskCommentResponse mapToCommentResponse(TaskComment comment) {

        // Map người bình luận
        TaskCommentResponse.CommentUserResponse commentUser = TaskCommentResponse.CommentUserResponse.builder()
                .userId(comment.getUser().getId())
                .fullName(comment.getUser().getFullName())
                .avatarUrl(comment.getUser().getAvatarUrl())
                .build();

        // SỬA: XÓA BỎ MAP DANH SÁCH MENTION
        /*
         List<TaskCommentResponse.CommentUserResponse> mentionedUsersList = comment.getMentionedUsers().stream()
                 .map(user -> TaskCommentResponse.CommentUserResponse.builder()
                         .userId(user.getId())
                         .fullName(user.getFullName())
                         .avatarUrl(user.getAvatarUrl())
                         .build())
                 .collect(Collectors.toList());
         */

        // Xây dựng DTO Response cuối cùng
        return TaskCommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .user(commentUser) // Thông tin người viết
                // .mentionedUsers(mentionedUsersList) // SỬA: Xóa
                .build();
    }
}