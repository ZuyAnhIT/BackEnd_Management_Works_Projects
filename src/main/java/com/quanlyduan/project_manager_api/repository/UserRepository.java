// File: src/main/java/com/quanlyduan/project_manager_api/repository/UserRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.User; // Entity Người dùng
import org.springframework.data.jpa.repository.JpaRepository;
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
}