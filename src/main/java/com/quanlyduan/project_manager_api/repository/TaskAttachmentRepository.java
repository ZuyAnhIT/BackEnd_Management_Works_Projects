// File: src/main/java/com/quanlyduan/project_manager_api/repository/TaskAttachmentRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.TaskAttachment; // Entity Tệp đính kèm Task
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Repository cho Entity TaskAttachment (Quản lý các tệp đính kèm của Task).
 */
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Integer> {

    /**
     * Lấy danh sách tất cả các tệp đính kèm thuộc về một Task cụ thể.
     */
    List<TaskAttachment> findByTask_Id(Integer taskId);
}