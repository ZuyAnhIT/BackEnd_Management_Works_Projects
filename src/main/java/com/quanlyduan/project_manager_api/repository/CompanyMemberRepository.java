// File: src/main/java/com/quanlyduan/project_manager_api/repository/CompanyMemberRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CompanyMember; // Đã dịch
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Integer> { // Đã dịch
    
    boolean existsByCompany_IdAndUser_Email(Integer companyId, String email); // Đã dịch

    // Lấy tất cả thành viên của công ty
    List<CompanyMember> findByCompany_Id(Integer companyId); // Đã dịch

    // (Bảo mật) Kiểm tra xem user có phải là thành viên không
    boolean existsByCompany_IdAndUser_Id(Integer companyId, Integer userId); // Đã dịch


    Optional<CompanyMember> findByCompany_IdAndUser_Id(Integer companyId, Integer userId); // Đã dịch
    
    
    List<CompanyMember> findByUser_Id(Integer userId); // Đã dịch

    @Query("""
            SELECT cm FROM CompanyMember cm
            WHERE cm.user.id = :userId
              AND cm.company.status IN :allowedStatuses
            """)
    List<CompanyMember> findByUserIdAndCompanyStatuses(@Param("userId") Integer userId,
                                                       @Param("allowedStatuses") List<CompanyStatus> allowedStatuses);

    boolean existsByCompany_IdAndUser_IdAndRole_RoleCode(Integer companyId, Integer userId, String roleCode);

    boolean existsByCompany_IdAndUser_IdAndRole_RoleCodeIn(Integer companyId, Integer userId, Set<String> roleCodes);
    /**
     * Tìm thành viên đang HOẠT ĐỘNG theo Company ID và User ID.
     * Dùng cho kiểm tra bảo mật.
     */
    Optional<CompanyMember> findByCompany_IdAndUser_IdAndStatus(Integer companyId, Integer userId, MemberStatus status);

    
    /**
     * Kiểm tra thành viên HOẠT ĐỘNG có tồn tại không.
     */
    boolean existsByCompany_IdAndUser_IdAndStatus(Integer companyId, Integer userId, MemberStatus status);

    @Query("SELECT COUNT(p.id) > 0 FROM CompanyMember cm " +
           "JOIN cm.role r " +
           "JOIN r.permissions p " +
           "WHERE cm.user.id = :userId " +
           "AND cm.company.id = :companyId " +
           "AND p.permissionCode = :permissionCode")
    boolean checkCompanyPermission(@Param("userId") Integer userId,
                                   @Param("companyId") Integer companyId,
                                   @Param("permissionCode") String permissionCode);


    /**
     * Tìm các thành viên công ty của user,
     * lọc theo danh sách trạng thái CÔNG TY (không phải trạng thái thành viên).
     */
    @Query("SELECT cm FROM CompanyMember cm " +
           "JOIN FETCH cm.company c " +
           "JOIN FETCH cm.role r " +
           "WHERE cm.user.id = :userId AND c.status IN :statuses")
    List<CompanyMember> findByUser_IdAndCompany_StatusIn(
        @Param("userId") Integer userId, 
        @Param("statuses") Collection<CompanyStatus> statuses
    );
}
