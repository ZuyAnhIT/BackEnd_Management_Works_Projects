// File: src/main/java/com/quanlyduan/project_manager_api/repository/RoleRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Chúng ta sẽ dùng mã Role để tìm, ví dụ: "COMPANY_ADMIN"
    Optional<Role> findFirstByRoleCode(String roleCode);
    Optional<Role> findByRoleCodeAndLevel(String roleCode, RoleLevel level);

//     // === CÁC TRUY VẤN PERMISSION MỚI ===

//     /**
//      * Kiểm tra quyền hạn cấp CÔNG TY
//      */
//     @Query("SELECT COUNT(p.id) > 0 FROM CompanyMember cm " +
//            "JOIN cm.role r " +
//            "JOIN r.permissions p " + // SỬA: Điều hướng trực tiếp
//            "WHERE cm.user.id = :userId " +
//            "AND cm.company.id = :companyId " +
//            "AND p.permissionCode = :permissionCode")
//     boolean checkCompanyPermission(@Param("userId") Integer userId,
//                                    @Param("companyId") Integer companyId,
//                                    @Param("permissionCode") String permissionCode);

//     /**
//      * Kiểm tra quyền hạn cấp WORKSPACE
//      */
//     @Query("SELECT COUNT(p.id) > 0 FROM WorkspaceMember wm " +
//            "JOIN wm.role r " +
//            "JOIN r.permissions p " + // SỬA: Điều hướng trực tiếp
//            "WHERE wm.user.id = :userId " +
//            "AND wm.workspace.id = :workspaceId " +
//            "AND p.permissionCode = :permissionCode")
//     boolean checkWorkspacePermission(@Param("userId") Integer userId,
//                                      @Param("workspaceId") Integer workspaceId,
//                                      @Param("permissionCode") String permissionCode);

//     /**
//      * Kiểm tra quyền hạn cấp PROJECT
//      */
//     @Query("SELECT COUNT(p.id) > 0 FROM ProjectMember pm " +
//            "JOIN pm.role r " +
//            "JOIN r.permissions p " + // SỬA: Điều hướng trực tiếp
//            "WHERE pm.user.id = :userId " +
//            "AND pm.project.id = :projectId " +
//            "AND p.permissionCode = :permissionCode")
//     boolean checkProjectPermission(@Param("userId") Integer userId,
//                                    @Param("projectId") Integer projectId,
//                                    @Param("permissionCode") String permissionCode);

//     public boolean checkSystemPermission(Integer userId, String permissionCode);

}