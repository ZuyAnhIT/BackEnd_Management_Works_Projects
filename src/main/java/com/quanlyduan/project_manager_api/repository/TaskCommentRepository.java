package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer> {

    /**
     * Lấy tất cả bình luận của một Task, sắp xếp theo thời gian tạo cũ nhất
     * (để hiển thị đúng thứ tự trong chat).
     * * @param taskId ID của Task cần lấy bình luận
     * @return Danh sách các bình luận
     */
    List<TaskComment> findByTask_IdOrderByCreatedAtAsc(Integer taskId);
}