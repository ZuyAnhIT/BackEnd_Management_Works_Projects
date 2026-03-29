package com.quanlyduan.project_manager_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.TaskComment;

/**
 * Kho lưu trữ dữ liệu quản lý các bình luận của công việc (Task Comments).
 * Hỗ trợ truy xuất lịch sử trao đổi và thảo luận trong phạm vi từng nhiệm vụ cụ thể.
 */
@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer> {

    // ======================================================
    // 1. TRUY VẤN DỮ LIỆU (RETRIEVAL)
    // ======================================================

    /**
     * Lấy toàn bộ danh sách bình luận của một công việc theo thứ tự thời gian tăng dần.
     * Việc sắp xếp tăng dần (Oldest first) giúp hiển thị đúng luồng lịch sử hội thoại/chat.
     * @param taskId ID của công việc cần truy vấn bình luận.
     * @return Danh sách các bình luận đã được sắp xếp.
     */
    List<TaskComment> findByTask_IdOrderByCreatedAtAsc(Integer taskId);
}