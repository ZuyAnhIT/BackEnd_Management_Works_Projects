// File: src/main/java/com/quanlyduan/project_manager_api/repository/WorkspaceMemberRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.WorkspaceMember; // Đã dịch
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer> { // Đã dịch

    // *** THÊM PHƯƠNG THỨC NÀY ***
    /**
     * Tìm kiếm tư cách thành viên dựa trên ID không gian và ID người dùng.
     * @param khongGianId ID của không gian
     * @param nguoiDungId ID của người dùng
     * @return Optional<KhongGianThanhVien>
     */
    Optional<WorkspaceMember> findByWorkspace_IdAndUser_Id( // Đã dịch
        Integer workspaceId, Integer userId // Đã dịch
    );

    List<WorkspaceMember> findByUser_Id(Integer userId); // Đã dịch
    
    /**
     * Tìm thành viên không gian đang HOẠT ĐỘNG.
     * Dùng cho kiểm tra bảo mật.
     */
    Optional<WorkspaceMember> findByWorkspace_IdAndUser_IdAndStatus(
        Integer workspaceId, Integer userId, MemberStatus status
    );

     // (Mới)
    boolean existsByWorkspace_IdAndUser_Id(Integer workspaceId, Integer userId);

    boolean existsByWorkspace_IdAndUser_IdAndRole_RoleCode(
        Integer workspaceId, Integer userId, String roleCode
    );

    /**
     * Lấy tất cả thành viên (bất kể trạng thái) của một không gian.
     */
    List<WorkspaceMember> findByWorkspace_Id(Integer workspaceId);

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