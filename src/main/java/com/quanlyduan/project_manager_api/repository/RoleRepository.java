// File: src/main/java/com/quanlyduan/project_manager_api/repository/RoleRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Role; // Entity Vai trò
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
/**
 * Repository cho Entity Role (Quản lý các Vai trò và Quyền hạn).
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Tìm kiếm Vai trò theo mã Role Code.
     * Thường dùng để lấy Role mặc định khi đăng ký/thêm thành viên.
     */
    Optional<Role> findFirstByRoleCode(String roleCode);

    /**
     * Tìm kiếm Vai trò theo mã Role Code và Cấp độ (Level).
     */
    Optional<Role> findByRoleCodeAndLevel(String roleCode, RoleLevel level);


}