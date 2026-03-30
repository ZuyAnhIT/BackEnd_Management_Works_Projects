package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class TaskCommentServiceImpl implements TaskCommentService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ACTION_COMMENT = "COMMENT";
    public static final String ENTITY_TASK = "TASK";
    public static final String DESC_COMMENT_TASK = "Comment on task";

    public static final String ERROR_USER_NOT_FOUND = "Current user not found.";
    public static final String ERROR_TASK_NOT_FOUND = "Task not found with ID: ";

    // Khai bao cac bien phu thuoc
    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;

    // Constructor khoi tao thu cong
    public TaskCommentServiceImpl(TaskCommentRepository commentRepository,
                                  TaskRepository taskRepository,
                                  UserRepository userRepository,
                                  SecurityService securityService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional
    @LogActivity(action = ACTION_COMMENT, entityType = ENTITY_TASK, description = DESC_COMMENT_TASK)
    public TaskCommentResponse addComment(Integer taskId, CommentRequest request) {
        // Lay thong tin nguoi dung hien tai dang thuc hien binh luan
        Integer currentUserId = securityService.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND));

        // Kiem tra su ton tai cua cong viec duoc binh luan
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_TASK_NOT_FOUND + taskId));

        // Khoi tao thuc the binh luan moi
        TaskComment newComment = TaskComment.builder()
                .content(request.getContent())
                .task(task)
                .user(currentUser)
                .build();

        // Luu vao co so du lieu
        TaskComment savedComment = commentRepository.save(newComment);

        return mapToCommentResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskCommentResponse> getComments(Integer taskId) {
        // Kiem tra su ton tai cua cong viec truoc khi truy xuat binh luan
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException(ERROR_TASK_NOT_FOUND + taskId);
        }

        // Lay danh sach binh luan tu he thong, sap xep theo thoi gian tao tang dan
        List<TaskComment> comments = commentRepository.findByTask_IdOrderByCreatedAtAsc(taskId);

        // Chuyen doi danh sach thuc the sang DTO phan hoi
        return comments.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private TaskCommentResponse mapToCommentResponse(TaskComment comment) {
        // Chuyen doi thong tin cua nguoi binh luan
        TaskCommentResponse.CommentUserResponse commentUser = TaskCommentResponse.CommentUserResponse.builder()
                .userId(comment.getUser().getId())
                .fullName(comment.getUser().getFullName())
                .avatarUrl(comment.getUser().getAvatarUrl())
                .build();

        // Xay dung doi tuong phan hoi chi tiet cho binh luan
        return TaskCommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .user(commentUser) 
                .projectId(comment.getTask().getProject().getId())
                .workspaceId(comment.getTask().getProject().getWorkspace().getId())
                .companyId(comment.getTask().getProject().getWorkspace().getCompany().getId())
                .build();
    }
}