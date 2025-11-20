package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Repository cho Project – phục vụ kiểm tra unique và truy vấn theo workspace.
 */
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    boolean existsByWorkspace_IdAndProjectCodeIgnoreCase(Integer workspaceId, String projectCode);

    /**
     * Lấy danh sách dự án trong workspace (Có phân trang).
     * Spring Data JPA tự động xử lý LIMIT/OFFSET.
     */
    Page<Project> findByWorkspace_Id(Integer workspaceId, Pageable pageable);
    
    // ProjectRepository
    @Query("SELECT p.id FROM Project p WHERE p.workspace.id = :workspaceId")
    List<Integer> findProjectIdsByWorkspaceId(@Param("workspaceId") Integer workspaceId);

}
