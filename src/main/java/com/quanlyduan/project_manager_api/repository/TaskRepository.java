package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    // Spring Data JPA tự động cung cấp 'findById(Integer taskId)'.
    // Hàm này là đủ để SecurityService tìm Task và lấy 'projectId' từ nó.

    // 'existsById(Integer taskId)' cũng được cung cấp,
    // dùng để TaskCommentServiceImpl kiểm tra Task có tồn tại không.
}