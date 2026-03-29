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

import com.quanlyduan.project_manager_api.model.ProjectMember;

/**
 * Kho luu tru du lieu quan ly thanh vien du an (Project Member).
 * Ho tro kiem tra quyen han, vai tro va truy van danh sach nhan su trong du an.
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer>, JpaSpecificationExecutor<ProjectMember> {

    // Khai bao cau truy van kiem tra quyen han (Permission-based Auth)
    String CHECK_PERMISSION_QUERY = "SELECT COUNT(p.id) > 0 FROM ProjectMember pm " +
                                    "JOIN pm.role r " +
                                    "JOIN r.permissions p " +
                                    "WHERE pm.user.id = :userId " +
                                    "AND pm.project.id = :projectId " +
                                    "AND p.permissionCode = :permissionCode";

    // ======================================================
    // 1. KIEM TRA QUYEN HAN VA VAI TRO (SECURITY)
    // ======================================================

    /**
     * Kiem tra nguoi dung co mot Quyen han (Permission) cu the trong du an hay khong.
     * Logic: Truy van thong qua quan he giua Thanh vien -> Vai tro -> Danh sach quyen.
     */
    @Query(CHECK_PERMISSION_QUERY)
    boolean checkProjectPermission(@Param("userId") Integer userId,
                                   @Param("projectId") Integer projectId,
                                   @Param("permissionCode") String permissionCode);

    /**
     * Kiem tra nguoi dung co vai tro cu the (Role Code) trong du an hay khong.
     */
    boolean existsByProject_IdAndUser_IdAndRole_RoleCode(Integer projectId, Integer userId, String roleCode);

    // ======================================================
    // 2. KIEM TRA SU TON TAI (EXISTENCE)
    // ======================================================

    /**
     * Kiem tra xem nguoi dung co phai la thanh vien cua du an hay khong.
     */
    boolean existsByProject_IdAndUser_Id(Integer projectId, Integer userId);

    // ======================================================
    // 3. TRUY VAN DU LIEU (RETRIEVAL)
    // ======================================================

    /**
     * Tim chi tiet thong tin thanh vien dua tren ID du an va ID nguoi dung.
     * Dung cho cac thao tac cap nhat vai tro hoac xoa thanh vien khoi du an.
     */
    Optional<ProjectMember> findByProject_IdAndUser_Id(Integer projectId, Integer userId);

    /**
     * Lay danh sach thanh vien cua mot du an ho tro phan trang.
     */
    Page<ProjectMember> findByProject_Id(Integer projectId, Pageable pageable);

    /**
     * Lay toan bo danh sach thanh vien trong mot du an.
     * Dung cho cac tac vu thong ke hoay AI Analytics.
     */
    List<ProjectMember> findByProject_Id(Integer projectId);

    /**
     * Lay danh sach tat ca cac moi quan he thanh vien du an cua mot nguoi dung.
     */
    List<ProjectMember> findByUser_Id(Integer userId);
}