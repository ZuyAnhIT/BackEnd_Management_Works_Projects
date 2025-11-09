package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository cho Project – phục vụ kiểm tra unique và truy vấn theo workspace.
 */
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    boolean existsByWorkspace_IdAndProjectCodeIgnoreCase(Integer workspaceId, String projectCode);

    List<Project> findByWorkspace_Id(Integer workspaceId);
}

