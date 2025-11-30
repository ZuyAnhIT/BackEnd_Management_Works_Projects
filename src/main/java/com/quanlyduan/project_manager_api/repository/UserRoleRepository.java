// File: src/main/java/com/quanlyduan/project_manager_api/repository/UserRoleRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Role; // Entity Vai trò
import com.quanlyduan.project_manager_api.model.User; // Entity Người dùng
import com.quanlyduan.project_manager_api.model.UserRole; // Entity Quan hệ Người dùng - Vai trò
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Repository cho Entity UserRole (Quản lý các Vai trò cấp Hệ thống được gán cho User).
 */
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {

    /**
     * Tìm tất cả các mối quan hệ vai trò (UserRole) của một người dùng.
     * @param userId ID của người dùng
     * @return Danh sách các UserRole
     */
    List<UserRole> findByUser_Id(Integer userId);

    /**
     * Kiểm tra xem User có một Quyền hạn (Permission) cấp hệ thống cụ thể không.
     * Logic: JOIN qua Role để xem có Permission tương ứng không.
     */
    @Query("SELECT COUNT(p.id) > 0 FROM UserRole ur " + // Bảng user_roles
            "JOIN ur.role r " +
            "JOIN r.permissions p " +
            "WHERE ur.user.id = :userId " +
            "AND p.permissionCode = :permissionCode")
    boolean checkSystemPermission(@Param("userId") Integer userId,
                                  @Param("permissionCode") String permissionCode);

    /**
     * Kiểm tra xem mối quan hệ giữa User và Role cụ thể đã tồn tại chưa.
     */
    boolean existsByUserAndRole(User user, Role userRole);
}