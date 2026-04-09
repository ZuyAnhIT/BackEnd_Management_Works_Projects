package com.quanlyduan.project_manager_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.User;

/**
 * Kho lưu trữ dữ liệu quản lý thông tin Người dùng (User).
 * Hỗ trợ các thao tác xác thực tài khoản và kiểm tra quyền hạn cấp hệ thống (Platform Admin).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Khai báo câu truy vấn Native SQL để kiểm tra quyền hạn cấp SYSTEM
    String COUNT_SYSTEM_PERMISSION_QUERY = 
            "SELECT COUNT(*) " +
            "FROM user_roles ur " +
            "JOIN roles r ON ur.role_id = r.id " +
            "JOIN role_permissions rp ON r.id = rp.role_id " +
            "JOIN permissions p ON rp.permission_id = p.id " +
            "WHERE ur.user_id = :userId " +
            "  AND p.permission_code = :permissionCode " +
            "  AND r.level = 'SYSTEM'";

    // ======================================================
    // 1. XÁC THỰC VÀ TÌM KIẾM (AUTHENTICATION)
    // ======================================================

    /**
     * Tìm kiếm người dùng dựa trên địa chỉ Email.
     * Sử dụng chủ yếu cho luồng đăng nhập và cấp phát mã xác thực.
     * @param email Địa chỉ email của người dùng.
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra sự tồn tại của địa chỉ Email trong hệ thống.
     * Thường dùng để ràng buộc tính duy nhất khi đăng ký tài khoản mới.
     */
    boolean existsByEmail(String email);

    // ======================================================
    // 2. PHÂN QUYỀN HỆ THỐNG (SYSTEM AUTHORIZATION)
    // ======================================================

    /**
     * Kiểm tra xem người dùng có sở hữu một quyền hạn cụ thể ở cấp độ Hệ thống (SYSTEM) hay không.
     * Thực hiện JOIN qua các bảng: user_roles, roles, role_permissions và permissions.
     * @param userId ID của người dùng cần kiểm tra.
     * @param permissionCode Mã quyền hạn (ví dụ: MANAGE_COMPANIES).
     * @return Số lượng quyền hạn tìm thấy (thường là 0 hoặc 1).
     */
    @Query(value = COUNT_SYSTEM_PERMISSION_QUERY, nativeQuery = true)
    int countSystemPermission(@Param("userId") Integer userId, 
                              @Param("permissionCode") String permissionCode);

     String COUNT_ALL_USERS = "SELECT COUNT(u) FROM User u";

    /**
     * Count all registered users across the entire system.
     */
    @Query(COUNT_ALL_USERS)
    long countTotalUsers();

    
    String SEARCH_GLOBAL_USERS = "SELECT u FROM User u " +
            "WHERE (:keyword IS NULL OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR u.status = :status)";

    /**
     * Tìm kiếm người dùng toàn cục trên hệ thống (Dành cho Super Admin).
     */
    @Query(SEARCH_GLOBAL_USERS)
    org.springframework.data.domain.Page<User> searchGlobalUsers(
            @org.springframework.data.repository.query.Param("keyword") String keyword, 
            @org.springframework.data.repository.query.Param("status") com.quanlyduan.project_manager_api.model.common.enums.UserStatus status, // <--- Sửa dòng này thành Enum
            org.springframework.data.domain.Pageable pageable);

    
    
}