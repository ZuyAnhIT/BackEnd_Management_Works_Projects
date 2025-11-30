// File: src/main/java/com/quanlyduan/project_manager_api/repository/ProjectTypeRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ProjectType; // Entity Loại Dự án
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repository cho Entity ProjectType (Quản lý các loại dự án).
 * Repository này cung cấp các phương thức CRUD cơ bản.
 * Hỗ trợ optional projectTypeId khi tạo Project.
 */
public interface ProjectTypeRepository extends JpaRepository<ProjectType, Integer> {
    // Repository này sử dụng các phương thức mặc định của JpaRepository (findById, findAll, save, delete...)
}