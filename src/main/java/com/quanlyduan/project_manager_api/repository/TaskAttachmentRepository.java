// File: src/main/java/com/quanlyduan/project_manager_api/repository/TaskAttachmentRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Integer> {
    
    List<TaskAttachment> findByTask_Id(Integer taskId);
}
