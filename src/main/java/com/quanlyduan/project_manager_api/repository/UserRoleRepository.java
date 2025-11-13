// File: src/main/java/com/quanlyduan/project_manager_api/repository/UserRoleRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.UserRole; // Đã dịch
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> { // Đã dịch

    /**
     * Tìm tất cả các vai trò cấp hệ thống của một người dùng.
     * @param nguoiDungId ID của người dùng
     * @return Danh sách các NguoiDungRole
     */
    List<UserRole> findByUser_Id(Integer userId); // Đã dịch
    
    @Query("SELECT COUNT(p.id) > 0 FROM UserRole ur " + // Bảng user_roles
           "JOIN ur.role r " +
           "JOIN r.permissions p " +
           "WHERE ur.user.id = :userId " +
           "AND p.permissionCode = :permissionCode")
    boolean checkSystemPermission(@Param("userId") Integer userId,
                                  @Param("permissionCode") String permissionCode);

    boolean existsByUserAndRole(User user, Role userRole);
}