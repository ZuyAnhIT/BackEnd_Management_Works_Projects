// File: src/main/java/com/quanlyduan/project_manager_api/repository/UserRoleRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.UserRole; // Đã dịch
import org.springframework.data.jpa.repository.JpaRepository;
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
}