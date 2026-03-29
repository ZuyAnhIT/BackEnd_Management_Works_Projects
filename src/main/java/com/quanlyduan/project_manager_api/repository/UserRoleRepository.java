package com.quanlyduan.project_manager_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.UserRole;

/**
 * Kho lưu trữ dữ liệu quản lý quan hệ Người dùng - Vai trò hệ thống (User Role).
 * Hỗ trợ kiểm tra quyền hạn mức Platform và quản lý danh sách vai trò được gán cho User.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {

    // Khai báo hằng số cho câu truy vấn JPQL kiểm tra quyền hạn hệ thống
    String CHECK_SYSTEM_PERMISSION_QUERY = "SELECT COUNT(p.id) > 0 FROM UserRole ur " +
                                           "JOIN ur.role r " +
                                           "JOIN r.permissions p " +
                                           "WHERE ur.user.id = :userId " +
                                           "AND p.permissionCode = :permissionCode";

    // ======================================================
    // 1. KIỂM TRA QUYỀN HẠN HỆ THỐNG (AUTHORIZATION)
    // ======================================================

    /**
     * Kiểm tra xem người dùng có sở hữu một Quyền hạn (Permission) cấp hệ thống cụ thể hay không.
     * Thực hiện truy vấn thông qua quan hệ giữa UserRole -> Role -> Permissions.
     * @param userId ID của người dùng cần kiểm tra.
     * @param permissionCode Mã quyền hạn hệ thống (ví dụ: MANAGE_USERS).
     * @return true nếu người dùng có quyền tương ứng.
     */
    @Query(CHECK_SYSTEM_PERMISSION_QUERY)
    boolean checkSystemPermission(@Param("userId") Integer userId,
                                  @Param("permissionCode") String permissionCode);

    // ======================================================
    // 2. TRUY VẤN DỮ LIỆU (RETRIEVAL)
    // ======================================================

    /**
     * Lấy danh sách tất cả các mối quan hệ vai trò hệ thống của một người dùng.
     * @param userId ID của người dùng.
     * @return Danh sách các đối tượng UserRole tìm thấy.
     */
    List<UserRole> findByUser_Id(Integer userId);

    // ======================================================
    // 3. KIỂM TRA RÀNG BUỘC (VALIDATION)
    // ======================================================

    /**
     * Kiểm tra xem một vai trò cụ thể đã được gán cho người dùng hay chưa.
     * Sử dụng để tránh việc gán trùng lặp vai trò hệ thống cho cùng một người dùng.
     */
    boolean existsByUserAndRole(User user, Role userRole);
}