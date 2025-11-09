package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.repository.projection.ProjectView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    // Kiem tra ton tai project theo (workspace, projectCode) bang JPA (khong dung cho soft delete)
    boolean existsByWorkspace_IdAndProjectCodeIgnoreCase(Integer workspaceId, String projectCode);

    @Query("SELECT p FROM Project p WHERE p.workspace.id = :workspaceId AND LOWER(p.projectCode) = LOWER(:projectCode)")
    Optional<Project> findByWorkspaceIdAndProjectCodeIgnoreCase(@Param("workspaceId") Integer workspaceId,
                                                                @Param("projectCode") String projectCode);

    // US7: Insert project moi (ho tro JSON/nullable, default priority)
    @Modifying
    @Query(value = "INSERT INTO projects (workspace_id, project_type_id, name, project_code, description, cover_image_url, goal, manager_id, status, priority, start_date, due_date, progress, created_by_id, board_config, created_at, updated_at) " +
            "VALUES (:workspaceId, :projectTypeId, :name, :projectCode, :description, :coverImageUrl, :goal, :managerId, 'NEW', COALESCE(:priority, 'MEDIUM'), :startDate, :dueDate, 0.00, :createdById, :boardConfig, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            nativeQuery = true)
    int insertProject(@Param("workspaceId") Integer workspaceId,
                      @Param("projectTypeId") Integer projectTypeId,
                      @Param("name") String name,
                      @Param("projectCode") String projectCode,
                      @Param("description") String description,
                      @Param("coverImageUrl") String coverImageUrl,
                      @Param("goal") String goal,
                      @Param("managerId") Integer managerId,
                      @Param("priority") String priority,
                      @Param("startDate") LocalDate startDate,
                      @Param("dueDate") LocalDate dueDate,
                      @Param("createdById") Integer createdById,
                      @Param("boardConfig") String boardConfig);

    // Lay ID vua tao theo (workspace, project_code)
    @Query(value = "SELECT id FROM projects WHERE workspace_id = :workspaceId AND LOWER(project_code) = LOWER(:projectCode) ORDER BY id DESC LIMIT 1",
            nativeQuery = true)
    Integer findIdByWorkspaceAndProjectCode(@Param("workspaceId") Integer workspaceId,
                                            @Param("projectCode") String projectCode);

    // US7: Dem project_types de validate projectTypeId (neu co)
    @Query(value = "SELECT COUNT(*) FROM project_types WHERE id = :id", nativeQuery = true)
    long countProjectTypeById(@Param("id") Integer id);

    // US7: Dem so project con hoat dong (chua xoa) trung project_code trong workspace
    @Query(value = "SELECT COUNT(*) FROM projects WHERE workspace_id = :workspaceId AND LOWER(project_code) = LOWER(:projectCode) AND deleted_at IS NULL",
            nativeQuery = true)
    long countActiveProjectCode(@Param("workspaceId") Integer workspaceId,
                                @Param("projectCode") String projectCode);

    // US8: Danh sach project theo workspace (chi project chua xoa)
    @Query(value = "SELECT p.id as id, p.workspace_id as workspaceId, p.project_type_id as projectTypeId, p.name as name, " +
            "p.project_code as projectCode, p.description as description, p.cover_image_url as coverImageUrl, p.goal as goal, " +
            "p.status as status, p.priority as priority, p.start_date as startDate, p.due_date as dueDate, " +
            "p.manager_id as managerId, p.created_by_id as createdById, p.board_config as boardConfig, " +
            "p.created_at as createdAt, p.updated_at as updatedAt " +
            "FROM projects p JOIN workspaces w ON w.id = p.workspace_id " +
            "WHERE p.workspace_id = :workspaceId AND w.company_id = :companyId AND p.deleted_at IS NULL " +
            "ORDER BY p.created_at DESC",
            nativeQuery = true)
    List<ProjectView> findProjectsByWorkspace(@Param("companyId") Integer companyId,
                                              @Param("workspaceId") Integer workspaceId);

    // US8: Chi tiet project (chi project chua xoa)
    @Query(value = "SELECT p.id as id, p.workspace_id as workspaceId, p.project_type_id as projectTypeId, p.name as name, " +
            "p.project_code as projectCode, p.description as description, p.cover_image_url as coverImageUrl, p.goal as goal, " +
            "p.status as status, p.priority as priority, p.start_date as startDate, p.due_date as dueDate, " +
            "p.manager_id as managerId, p.created_by_id as createdById, p.board_config as boardConfig, " +
            "p.created_at as createdAt, p.updated_at as updatedAt " +
            "FROM projects p JOIN workspaces w ON w.id = p.workspace_id " +
            "WHERE p.id = :projectId AND p.workspace_id = :workspaceId AND w.company_id = :companyId AND p.deleted_at IS NULL " +
            "LIMIT 1",
            nativeQuery = true)
    ProjectView findProjectDetail(@Param("companyId") Integer companyId,
                                  @Param("workspaceId") Integer workspaceId,
                                  @Param("projectId") Integer projectId);

    // US9: Xoa mem project (set deleted_at/deleted_by/ly do)
    @Modifying
    @Query(value = "UPDATE projects p JOIN workspaces w ON w.id = p.workspace_id " +
            "SET p.deleted_at = CURRENT_TIMESTAMP, p.deleted_by_id = :deletedById, p.deleted_reason = :reason, p.updated_at = CURRENT_TIMESTAMP " +
            "WHERE p.id = :projectId AND p.workspace_id = :workspaceId AND w.company_id = :companyId AND p.deleted_at IS NULL",
            nativeQuery = true)
    int softDeleteProject(@Param("companyId") Integer companyId,
                          @Param("workspaceId") Integer workspaceId,
                          @Param("projectId") Integer projectId,
                          @Param("deletedById") Integer deletedById,
                          @Param("reason") String reason);

    // US9: Doi ten project (IDOR trong SQL)
    @Modifying
    @Query(value = "UPDATE projects p JOIN workspaces w ON w.id = p.workspace_id " +
            "SET p.name = :name, p.updated_at = CURRENT_TIMESTAMP " +
            "WHERE p.id = :projectId AND p.workspace_id = :workspaceId AND w.company_id = :companyId",
            nativeQuery = true)
    int renameProject(@Param("companyId") Integer companyId,
                      @Param("workspaceId") Integer workspaceId,
                      @Param("projectId") Integer projectId,
                      @Param("name") String name);
}
