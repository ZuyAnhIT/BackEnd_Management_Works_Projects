// File: src/main/java/com/quanlyduan/project_manager_api/repository/ProjectMemberRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ProjectMember;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quanlyduan.project_manager_api.model.Project;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
    
    
    boolean existsByProject_IdAndUser_Id(Integer projectId, Integer userId);

    boolean existsByProject_IdAndUser_IdAndRole_RoleCode(
        Integer projectId, Integer userId, String roleCode
    );
    
    @Query("SELECT COUNT(p.id) > 0 FROM ProjectMember pm " +
           "JOIN pm.role r " +
           "JOIN r.permissions p " +
           "WHERE pm.user.id = :userId " +
           "AND pm.project.id = :projectId " +
           "AND p.permissionCode = :permissionCode")
    boolean checkProjectPermission(@Param("userId") Integer userId,
                                   @Param("projectId") Integer projectId,
                                   @Param("permissionCode") String permissionCode);

    // Dùng cho UserServiceImpl (lấy hồ sơ)
    List<ProjectMember> findByUser_Id(Integer userId);

    // Dùng cho chức năng MỚI (lấy danh sách)
    List<ProjectMember> findByProject_Id(Integer projectId);

    // Dùng cho các chức năng sau (cập nhật, xóa)
    Optional<ProjectMember> findByProject_IdAndUser_Id(Integer projectId, Integer userId);
    
}
