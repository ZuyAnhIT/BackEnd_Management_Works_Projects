package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ProjectType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cho ProjectType – hỗ trợ optional projectTypeId khi tạo Project.
 */
public interface ProjectTypeRepository extends JpaRepository<ProjectType, Integer> {
}

