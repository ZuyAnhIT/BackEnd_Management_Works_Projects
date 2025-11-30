// File: src/main/java/com/quanlyduan/project_manager_api/repository/TaskCommentRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.TaskComment; // Entity Bình luận Task
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
/**
 * Repository cho Entity TaskComment (Quản lý các bình luận/comment của Task).
 */
public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer> {

    /**
     * Lấy tất cả bình luận (comment) của một Task, sắp xếp theo thời gian tạo TĂNG DẦN
     * (CreatedAtAsc) để hiển thị đúng thứ tự lịch sử/chat.
     * @param taskId ID của Task cần lấy bình luận
     * @return Danh sách các bình luận
     */
    List<TaskComment> findByTask_IdOrderByCreatedAtAsc(Integer taskId);
}