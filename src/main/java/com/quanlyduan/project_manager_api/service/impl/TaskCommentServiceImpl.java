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
import com.quanlyduan.project_manager_api.security.SecurityServicePermission;
import com.quanlyduan.project_manager_api.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Tự động @Autowired các trường 'final'
public class TaskCommentServiceImpl implements TaskCommentService {

    // Tiêm các Repository và Service cần thiết
    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Dùng bean "securityServicePermission" để lấy thông tin user hiện tại
    private final SecurityServicePermission securityServicePermission;

    /**
     * Triển khai logic thêm bình luận
     */
    @Override
    @Transactional // Đảm bảo tất cả thao tác CSDL thành công hoặc thất bại cùng nhau
    public TaskCommentResponse addComment(Integer taskId, CommentRequest request) {

        // 1. Lấy user hiện tại (người bình luận)
        Integer currentUserId = securityServicePermission.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        // 2. Tìm Task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        // 3. Xử lý @mentions
        Set<User> mentionedUsers = new HashSet<>();
        if (request.getMentionedUserIds() != null && !request.getMentionedUserIds().isEmpty()) {
            // Tìm tất cả User có ID nằm trong danh sách
            List<User> foundUsers = userRepository.findAllById(request.getMentionedUserIds());
            mentionedUsers.addAll(foundUsers);

            // TODO: (Nâng cao) Gửi thông báo/email cho những người được mention
        }

        // 4. Tạo và lưu bình luận
        TaskComment newComment = TaskComment.builder()
                .content(request.getContent())
                .task(task)
                .user(currentUser)
                .mentionedUsers(mentionedUsers)
                .build();

        TaskComment savedComment = commentRepository.save(newComment);

        // 5. Map sang DTO và trả về
        return mapToCommentResponse(savedComment);
    }

    /**
     * Triển khai logic lấy danh sách bình luận
     */
    @Override
    @Transactional(readOnly = true) // readOnly=true để tối ưu cho các truy vấn GET
    public List<TaskCommentResponse> getComments(Integer taskId) {

        // 1. (Cẩn thận) Kiểm tra Task tồn tại
        // (Mặc dù @PreAuthorize đã chạy, kiểm tra này vẫn tốt)
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with ID: " + taskId);
        }

        // 2. Lấy danh sách bình luận từ CSDL (đã sắp xếp)
        List<TaskComment> comments = commentRepository.findByTask_IdOrderByCreatedAtAsc(taskId);

        // 3. Map danh sách Entity sang danh sách DTO
        return comments.stream()
                .map(this::mapToCommentResponse) // Sử dụng helper method
                .collect(Collectors.toList());
    }

    // --- Private Helper Methods ---

    /**
     * Hàm helper (nội bộ) để chuyển đổi Entity TaskComment (đầy đủ, nặng)
     * sang DTO TaskCommentResponse (gọn gàng, an toàn).
     */
    private TaskCommentResponse mapToCommentResponse(TaskComment comment) {

        // Map người bình luận
        TaskCommentResponse.CommentUserResponse commentUser = TaskCommentResponse.CommentUserResponse.builder()
                .userId(comment.getUser().getId())
                .fullName(comment.getUser().getFullName())
                .avatarUrl(comment.getUser().getAvatarUrl())
                .build();

        // Map danh sách người được mention
        List<TaskCommentResponse.CommentUserResponse> mentionedUsersList = comment.getMentionedUsers().stream()
                .map(user -> TaskCommentResponse.CommentUserResponse.builder()
                        .userId(user.getId())
                        .fullName(user.getFullName())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .collect(Collectors.toList());

        // Xây dựng DTO Response cuối cùng
        return TaskCommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .user(commentUser) // Thông tin người viết
                .mentionedUsers(mentionedUsersList) // Danh sách người bị tag
                .build();
    }
}