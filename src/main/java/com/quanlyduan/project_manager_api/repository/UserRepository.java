// File: src/main/java/com/quanlyduan/project_manager_api/repository/UserRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.User; // Entity Người dùng
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/**
 * Repository cho Entity User (Quản lý các thao tác với bảng users).
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Tìm kiếm người dùng theo địa chỉ Email.
     * Thường dùng trong quá trình đăng nhập và xác thực.
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra xem địa chỉ Email đã tồn tại trong hệ thống chưa.
     * Thường dùng trong quá trình đăng ký.
     */
    Boolean existsByEmail(String email);

    /**
     * [V2 - PLATFORM ADMIN] Kiểm tra quyền cấp Hệ thống (SYSTEM) của User.
     * JOIN qua 4 bảng để xác định User có giữ Role cấp SYSTEM và chứa Permission tương ứng hay không.
     */
    @Query(value = "SELECT COUNT(*) " +
            "FROM user_roles ur " +
            "JOIN roles r ON ur.role_id = r.id " +
            "JOIN role_permissions rp ON r.id = rp.role_id " +
            "JOIN permissions p ON rp.permission_id = p.id " +
            "WHERE ur.user_id = :userId " +
            "  AND p.permission_code = :permissionCode " +
            "  AND r.level = 'SYSTEM'", nativeQuery = true)
    int countSystemPermission(@Param("userId") Integer userId, @Param("permissionCode") String permissionCode);
}