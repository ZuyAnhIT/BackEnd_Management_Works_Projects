// File: src/main/java/com/quanlyduan/project_manager_api/repository/ProjectMemberRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ProjectMember; // Entity Thành viên Dự án

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository cho Entity ProjectMember (Quản lý mối quan hệ thành viên Dự án).
 */
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer>, JpaSpecificationExecutor<ProjectMember> {

    /**
     * Kiểm tra xem User có phải là thành viên (đã từng) của Dự án không.
     */
    boolean existsByProject_IdAndUser_Id(Integer projectId, Integer userId);

    /**
     * Kiểm tra xem User có vai trò cụ thể trong Dự án không.
     */
    boolean existsByProject_IdAndUser_IdAndRole_RoleCode(
        Integer projectId, Integer userId, String roleCode
    );

    /**
     * Kiểm tra xem User có một Quyền hạn (Permission) cụ thể trong Dự án không.
     * Logic: JOIN qua Role để xem có Permission tương ứng không.
     */
    @Query("SELECT COUNT(p.id) > 0 FROM ProjectMember pm " +
            "JOIN pm.role r " +
            "JOIN r.permissions p " +
            "WHERE pm.user.id = :userId " +
            "AND pm.project.id = :projectId " +
            "AND p.permissionCode = :permissionCode")
    boolean checkProjectPermission(@Param("userId") Integer userId,
                                   @Param("projectId") Integer projectId,
                                   @Param("permissionCode") String permissionCode);

    /**
     * Lấy tất cả các mối quan hệ thành viên (membership) của một User (dùng cho hồ sơ).
     */
    List<ProjectMember> findByUser_Id(Integer userId);

    /**
     * Lấy danh sách thành viên dự án theo Project ID (có phân trang).
     */
    Page<ProjectMember> findByProject_Id(Integer projectId, Pageable pageable);

    /**
     * Tìm kiếm chi tiết thành viên theo Project ID và User ID.
     * Dùng cho các chức năng cập nhật hoặc xóa thành viên cụ thể.
     */
    Optional<ProjectMember> findByProject_IdAndUser_Id(Integer projectId, Integer userId);

}