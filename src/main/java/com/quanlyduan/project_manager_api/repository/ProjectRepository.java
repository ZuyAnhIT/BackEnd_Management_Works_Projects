package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/** ProjectRepository: kiểm tra trùng mã, chèn project mới và truy vấn ID theo (workspaceId, projectCode). */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

    // Kiểm tra trùng mã dự án theo workspace (case-insensitive)
    boolean existsByWorkspace_IdAndProjectCodeIgnoreCase(Integer workspaceId, String projectCode);

    // Tìm theo workspaceId + projectCode (case-insensitive)
    @Query("SELECT p FROM Project p WHERE p.workspace.id = :workspaceId AND LOWER(p.projectCode) = LOWER(:projectCode)")
    Optional<Project> findByWorkspaceIdAndProjectCodeIgnoreCase(@Param("workspaceId") Integer workspaceId,
                                                                @Param("projectCode") String projectCode);

    /**
     * Native INSERT để chèn bản ghi mới vào bảng projects, bao gồm cột 'name'.
     * - Vì entity Project hiện không có field 'name', không thể dùng save(entity) theo cách thông thường.
     * - Trả về số dòng ảnh hưởng; sau khi insert, cần gọi findIdByWorkspaceAndProjectCode để lấy ID.
     */
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

    // Lấy ID dự án vừa tạo theo cặp (workspace_id, project_code). Trong trường hợp hiếm có trùng, ưu tiên ID mới nhất.
    @Query(value = "SELECT id FROM projects WHERE workspace_id = :workspaceId AND LOWER(project_code) = LOWER(:projectCode) ORDER BY id DESC LIMIT 1",
            nativeQuery = true)
    Integer findIdByWorkspaceAndProjectCode(@Param("workspaceId") Integer workspaceId,
                                           @Param("projectCode") String projectCode);

    // Đếm số record project_types theo id (tránh cast Boolean lỗi từ native query)
    @Query(value = "SELECT COUNT(*) FROM project_types WHERE id = :id", nativeQuery = true)
    long countProjectTypeById(@Param("id") Integer id);
}
