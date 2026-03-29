package com.quanlyduan.project_manager_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.WorkspaceMember;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

/**
 * Kho lưu trữ dữ liệu quản lý thành viên trong Không gian làm việc (Workspace).
 * Hỗ trợ các truy vấn về quan hệ nhân sự, kiểm tra quyền hạn và trạng thái hoạt động.
 */
@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer>, JpaSpecificationExecutor<WorkspaceMember> {

    // Khai báo hằng số cho câu truy vấn kiểm tra quyền hạn
    String CHECK_PERMISSION_QUERY = "SELECT COUNT(p.id) > 0 FROM WorkspaceMember wm " +
                                    "JOIN wm.role r " +
                                    "JOIN r.permissions p " +
                                    "WHERE wm.user.id = :userId " +
                                    "AND wm.workspace.id = :workspaceId " +
                                    "AND p.permissionCode = :permissionCode";

    // ======================================================
    // 1. KIỂM TRA QUYỀN HẠN VÀ VAI TRÒ (SECURITY)
    // ======================================================

    /**
     * Kiểm tra xem người dùng có một Quyền hạn (Permission) cụ thể trong Workspace không.
     * Logic thực hiện JOIN qua thực thể Role để kiểm tra mã quyền hạn tương ứng.
     */
    @Query(CHECK_PERMISSION_QUERY)
    boolean checkWorkspacePermission(@Param("userId") Integer userId,
                                     @Param("workspaceId") Integer workspaceId,
                                     @Param("permissionCode") String permissionCode);

    /**
     * Kiểm tra xem người dùng có sở hữu một vai trò cụ thể trong Workspace không.
     * @param roleCode Mã định danh của vai trò (ví dụ: WS_ADMIN).
     */
    boolean existsByWorkspace_IdAndUser_IdAndRole_RoleCode(Integer workspaceId, Integer userId, String roleCode);

    // ======================================================
    // 2. KIỂM TRA SỰ TỒN TẠI (VALIDATION)
    // ======================================================

    /**
     * Kiểm tra xem người dùng đã từng là thành viên của Workspace này chưa.
     */
    boolean existsByWorkspace_IdAndUser_Id(Integer workspaceId, Integer userId);

    // ======================================================
    // 3. TRUY VẤN DỮ LIỆU (RETRIEVAL)
    // ======================================================

    /**
     * Tìm kiếm thông tin thành viên dựa trên ID Workspace và ID người dùng.
     */
    Optional<WorkspaceMember> findByWorkspace_IdAndUser_Id(Integer workspaceId, Integer userId);

    /**
     * Tìm kiếm thành viên đang ở trạng thái HOẠT ĐỘNG (ACTIVE) trong Workspace.
     * Sử dụng cho các logic kiểm tra bảo mật thời gian thực.
     */
    Optional<WorkspaceMember> findByWorkspace_IdAndUser_IdAndStatus(Integer workspaceId, Integer userId, MemberStatus status);

    /**
     * Lấy danh sách thành viên của một Workspace hỗ trợ phân trang và sắp xếp.
     */
    Page<WorkspaceMember> findByWorkspace_Id(Integer workspaceId, Pageable pageable);

    /**
     * Lấy tất cả các mối quan hệ thành viên của một người dùng.
     * Thường dùng để hiển thị danh sách Workspace mà người dùng tham gia trên Dashboard hoặc Profile.
     */
    List<WorkspaceMember> findByUser_Id(Integer userId);
}