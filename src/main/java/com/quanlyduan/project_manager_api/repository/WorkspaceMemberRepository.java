// File: src/main/java/com/quanlyduan/project_manager_api/repository/WorkspaceMemberRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.WorkspaceMember; // Entity Thành viên Workspace
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
/**
 * Repository cho Entity WorkspaceMember (Quản lý mối quan hệ thành viên Workspace).
 * Kế thừa JpaSpecificationExecutor để hỗ trợ tìm kiếm động.
 */
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer>, JpaSpecificationExecutor<WorkspaceMember> {

    /**
     * Tìm kiếm mối quan hệ thành viên theo ID Workspace và ID người dùng.
     */
    Optional<WorkspaceMember> findByWorkspace_IdAndUser_Id(
        Integer workspaceId, Integer userId
    );

    /**
     * Lấy tất cả các mối quan hệ thành viên (membership) của một User.
     * Dùng cho Dashboard/Profile.
     */
    List<WorkspaceMember> findByUser_Id(Integer userId);

    /**
     * Tìm thành viên Workspace đang ở trạng thái HOẠT ĐỘNG (MemberStatus.ACTIVE).
     * Dùng cho kiểm tra bảo mật chi tiết.
     */
    Optional<WorkspaceMember> findByWorkspace_IdAndUser_IdAndStatus(
        Integer workspaceId, Integer userId, MemberStatus status
    );


    /**
     * Kiểm tra xem User đã từng là thành viên của Workspace này chưa.
     */
    boolean existsByWorkspace_IdAndUser_Id(Integer workspaceId, Integer userId);

    /**
     * Kiểm tra xem User có vai trò cụ thể trong Workspace không.
     */
    boolean existsByWorkspace_IdAndUser_IdAndRole_RoleCode(
        Integer workspaceId, Integer userId, String roleCode
    );

    /**
     * Lấy danh sách thành viên của một không gian (có phân trang).
     */
    Page<WorkspaceMember> findByWorkspace_Id(Integer workspaceId, Pageable pageable);

    /**
     * Kiểm tra xem User có một Quyền hạn (Permission) cụ thể trong Workspace không.
     * Logic: JOIN qua Role để xem có Permission tương ứng không.
     */
    @Query("SELECT COUNT(p.id) > 0 FROM WorkspaceMember wm " +
            "JOIN wm.role r " +
            "JOIN r.permissions p " +
            "WHERE wm.user.id = :userId " +
            "AND wm.workspace.id = :workspaceId " +
            "AND p.permissionCode = :permissionCode")
    boolean checkWorkspacePermission(@Param("userId") Integer userId,
                                     @Param("workspaceId") Integer workspaceId,
                                     @Param("permissionCode") String permissionCode);

}