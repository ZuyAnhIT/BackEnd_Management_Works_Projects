package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Integer> {

    /**
     * Lấy tất cả tệp đính kèm của một Task,
     * sắp xếp theo thời gian tải lên (ví dụ: cũ nhất trước).
     *
     * @param taskId ID của Task cần lấy tệp
     * @return Danh sách các tệp đính kèm
     */
    List<TaskAttachment> findByTask_IdOrderByUploadedAtAsc(Integer taskId);
}