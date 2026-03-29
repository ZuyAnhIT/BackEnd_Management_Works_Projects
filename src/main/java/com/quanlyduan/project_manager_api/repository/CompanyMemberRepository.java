package com.quanlyduan.project_manager_api.repository;

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

import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

/**
 * Kho lưu trữ dữ liệu quản lý thành viên công ty (Company Member).
 * Hỗ trợ các truy vấn về quan hệ nhân sự, kiểm tra quyền hạn và tìm kiếm động.
 */
@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Integer>, JpaSpecificationExecutor<CompanyMember> {

    // Khai báo các câu truy vấn JPQL để tránh hardcode trong phương thức
    String FIND_BY_USER_AND_COMPANY_STATUS = 
        "SELECT cm FROM CompanyMember cm " +
        "JOIN FETCH cm.company c " +
        "JOIN FETCH cm.role r " +
        "WHERE cm.user.id = :userId AND c.status IN :statuses";

    String CHECK_COMPANY_PERMISSION = 
        "SELECT COUNT(p.id) > 0 FROM CompanyMember cm " +
        "JOIN cm.role r " +
        "JOIN r.permissions p " +
        "WHERE cm.user.id = :userId " +
        "AND cm.company.id = :companyId " +
        "AND p.permissionCode = :permissionCode";

    String SEARCH_BY_KEYWORD = 
        "SELECT cm FROM CompanyMember cm " +
        "JOIN cm.user u " +
        "JOIN cm.role r " +
        "WHERE cm.company.id = :companyId " +
        "AND (:keyword IS NULL OR :keyword = '' OR " +
        "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        " LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        " LOWER(cm.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        " LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%'))))";

    // ======================================================
    // 1. KIỂM TRA SỰ TỒN TẠI VÀ ĐẾM (COUNT & EXISTS)
    // ======================================================

    /**
     * Đếm số lượng thành viên theo trạng thái trong một công ty.
     * Dùng để kiểm tra hạn mức (Quota) của gói dịch vụ.
     */
    long countByCompany_IdAndStatus(Integer companyId, MemberStatus status);

    /**
     * Đếm số lượng thành viên loại trừ một trạng thái cụ thể (ví dụ: loại bỏ thành viên đã xóa).
     */
    long countByCompany_IdAndStatusNot(Integer companyId, MemberStatus status);

    /**
     * Kiểm tra sự tồn tại của người dùng trong công ty qua Email.
     */
    boolean existsByCompany_IdAndUser_Email(Integer companyId, String email);

    /**
     * Kiểm tra sự tồn tại của người dùng trong công ty qua User ID.
     */
    boolean existsByCompany_IdAndUser_Id(Integer companyId, Integer userId);

    /**
     * Kiểm tra thành viên đang hoạt động có tồn tại không.
     */
    boolean existsByCompany_IdAndUser_IdAndStatus(Integer companyId, Integer userId, MemberStatus status);

    // ======================================================
    // 2. TRUY VẤN THÀNH VIÊN (FINDERS)
    // ======================================================

    /**
     * Tìm chi tiết thành viên dựa trên ID công ty và ID người dùng.
     */
    Optional<CompanyMember> findByCompany_IdAndUser_Id(Integer companyId, Integer userId);

    /**
     * Tìm thành viên đang hoạt động dựa trên ID công ty và ID người dùng.
     */
    Optional<CompanyMember> findByCompany_IdAndUser_IdAndStatus(Integer companyId, Integer userId, MemberStatus status);

    /**
     * Lấy danh sách tất cả các công ty mà một người dùng tham gia.
     */
    List<CompanyMember> findByUser_Id(Integer userId);

    /**
     * Lấy danh sách thành viên công ty kèm theo trạng thái của công ty đó.
     */
    @Query(FIND_BY_USER_AND_COMPANY_STATUS)
    List<CompanyMember> findByUser_IdAndCompany_StatusIn(
            @Param("userId") Integer userId,
            @Param("statuses") Collection<CompanyStatus> statuses
    );

    // ======================================================
    // 3. KIỂM TRA VAI TRÒ VÀ QUYỀN HẠN (SECURITY)
    // ======================================================

    /**
     * Kiểm tra người dùng có vai trò cụ thể trong công ty hay không.
     */
    boolean existsByCompany_IdAndUser_IdAndRole_RoleCode(Integer companyId, Integer userId, String roleCode);

    /**
     * Kiểm tra người dùng có thuộc bất kỳ vai trò nào trong danh sách cho trước hay không.
     */
    boolean existsByCompany_IdAndUser_IdAndRole_RoleCodeIn(Integer companyId, Integer userId, Set<String> roleCodes);

    /**
     * Kiểm tra người dùng có một quyền hạn cụ thể (Permission) trong công ty hay không.
     */
    @Query(CHECK_COMPANY_PERMISSION)
    boolean checkCompanyPermission(@Param("userId") Integer userId,
                                   @Param("companyId") Integer companyId,
                                   @Param("permissionCode") String permissionCode);

    // ======================================================
    // 4. TÌM KIẾM VÀ PHÂN TRANG (SEARCH & PAGINATION)
    // ======================================================

    /**
     * Lấy danh sách thành viên của một công ty hỗ trợ phân trang.
     */
    Page<CompanyMember> findByCompany_Id(Integer companyId, Pageable pageable);

    /**
     * Tìm kiếm thành viên theo từ khóa (Tên, Email, Chức danh, Vai trò).
     */
    @Query(SEARCH_BY_KEYWORD)
    Page<CompanyMember> searchByCompany_IdAndKeyword(
            @Param("companyId") Integer companyId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}