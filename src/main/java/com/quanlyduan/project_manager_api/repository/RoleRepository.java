package com.quanlyduan.project_manager_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;

/**
 * Kho lưu trữ dữ liệu quản lý Vai trò (Role).
 * Cung cấp các phương thức truy vấn để xác định tập hợp quyền hạn của người dùng ở nhiều cấp độ khác nhau.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Tìm kiếm vai trò dựa trên mã định danh Role Code.
     * Thường được sử dụng để lấy thông tin vai trò mặc định trong các luồng đăng ký hoặc mời thành viên.
     * @param roleCode Mã định danh của vai trò (Ví dụ: ROLE_ADMIN, ROLE_MEMBER).
     * @return Kết quả tìm kiếm dưới dạng Optional.
     */
    Optional<Role> findFirstByRoleCode(String roleCode);

    /**
     * Tìm kiếm vai trò dựa trên sự kết hợp giữa mã định danh và cấp độ phân quyền.
     * Giúp phân biệt các vai trò trùng mã nhưng khác cấp độ (Ví dụ: ADMIN cấp Công ty và ADMIN cấp Dự án).
     * @param roleCode Mã định danh của vai trò.
     * @param level Cấp độ của vai trò (SYSTEM, COMPANY, WORKSPACE, PROJECT).
     * @return Kết quả tìm kiếm dưới dạng Optional.
     */
    Optional<Role> findByRoleCodeAndLevel(String roleCode, RoleLevel level);

}