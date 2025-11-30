// File: src/main/java/com/quanlyduan/project_manager_api/repository/CompanyMemberRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CompanyMember; // Entity Thành viên Công ty
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
/**
 * Repository cho Entity CompanyMember (Quản lý mối quan hệ thành viên Công ty).
 * JpaSpecificationExecutor hỗ trợ tìm kiếm động (dynamic search).
 */
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Integer>, JpaSpecificationExecutor<CompanyMember> {

    /**
     * Kiểm tra xem đã có thành viên nào với Email này trong Công ty chưa.
     */
    boolean existsByCompany_IdAndUser_Email(Integer companyId, String email);

    /**
     * Kiểm tra xem User có phải là thành viên (đã từng) của Công ty không.
     */
    boolean existsByCompany_IdAndUser_Id(Integer companyId, Integer userId);

    /**
     * Tìm kiếm chi tiết thành viên theo Company ID và User ID.
     */
    Optional<CompanyMember> findByCompany_IdAndUser_Id(Integer companyId, Integer userId);

    /**
     * Lấy tất cả các mối quan hệ thành viên (membership) của một User.
     */
    List<CompanyMember> findByUser_Id(Integer userId);

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

    /**
     * Kiểm tra xem User có vai trò cụ thể trong Công ty không.
     */
    boolean existsByCompany_IdAndUser_IdAndRole_RoleCode(Integer companyId, Integer userId, String roleCode);

    /**
     * Kiểm tra xem User có bất kỳ vai trò nào trong danh sách cho trước trong Công ty không.
     */
    boolean existsByCompany_IdAndUser_IdAndRole_RoleCodeIn(Integer companyId, Integer userId, Set<String> roleCodes);

    /**
     * Tìm thành viên đang HOẠT ĐỘNG (MemberStatus.ACTIVE) theo Company ID và User ID.
     */
    Optional<CompanyMember> findByCompany_IdAndUser_IdAndStatus(Integer companyId, Integer userId, MemberStatus status);

    /**
     * Kiểm tra thành viên HOẠT ĐỘNG (MemberStatus.ACTIVE) có tồn tại không.
     */
    boolean existsByCompany_IdAndUser_IdAndStatus(Integer companyId, Integer userId, MemberStatus status);

    /**
     * Kiểm tra xem User có một Quyền hạn (Permission) cụ thể trong Công ty không.
     * Logic: JOIN qua Role để xem có Permission tương ứng không.
     */
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
     * Lấy danh sách thành viên theo Company ID (Có phân trang).
     */
    Page<CompanyMember> findByCompany_Id(Integer companyId, Pageable pageable);


    /**
     * Tìm kiếm thành viên trong công ty theo từ khóa.
     * Tìm trên: Tên, Email, Chức vụ (Job Title), Tên Vai trò (Role Name).
     * Sẽ bị thay thế bằng JpaSpecificationExecutor trong các logic nghiệp vụ nâng cao.
     */
    @Query("SELECT cm FROM CompanyMember cm " +
            "JOIN cm.user u " +
            "JOIN cm.role r " +
            "WHERE cm.company.id = :companyId " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(cm.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<CompanyMember> searchByCompany_IdAndKeyword(
            @Param("companyId") Integer companyId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}