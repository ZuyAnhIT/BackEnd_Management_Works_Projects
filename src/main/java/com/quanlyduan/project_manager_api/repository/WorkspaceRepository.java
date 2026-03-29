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

import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;

/**
 * Kho lưu trữ dữ liệu quản lý Không gian làm việc (Workspace).
 * Hỗ trợ các thao tác kiểm tra tên trùng lặp, quản lý hạn mức tài nguyên và tìm kiếm động.
 */
@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer>, JpaSpecificationExecutor<Workspace> {

    // Khai báo hằng số cho câu truy vấn JPQL lấy danh sách ID
    String FIND_IDS_BY_COMPANY_QUERY = "SELECT w.id FROM Workspace w WHERE w.company.id = :companyId";

    // ======================================================
    // 1. KIỂM TRA RÀNG BUỘC (VALIDATION)
    // ======================================================

    /**
     * Kiểm tra sự tồn tại của tên Workspace trong phạm vi một Công ty.
     * Sử dụng để đảm bảo tính duy nhất khi người dùng tạo mới Workspace.
     */
    boolean existsByCompany_IdAndName(Integer companyId, String workspaceName);

    /**
     * Tìm kiếm Workspace theo tên và ID Công ty.
     * Thường dùng để kiểm tra tính hợp lệ của tên khi thực hiện cập nhật thông tin.
     */
    Optional<Workspace> findByCompany_IdAndName(Integer companyId, String name);

    // ======================================================
    // 2. QUẢN LÝ HẠN MỨC DỮ LIỆU (QUOTA GUARD)
    // ======================================================

    /**
     * Đếm tổng số lượng Workspace của công ty, loại trừ các trạng thái không mong muốn (ví dụ: DELETED).
     * Phục vụ logic kiểm tra giới hạn của gói cước SaaS (Quota Guard).
     * @param companyId ID của công ty cần kiểm tra.
     * @param status Trạng thái cần loại trừ (ví dụ: WorkspaceStatus.DELETED).
     */
    long countByCompany_IdAndStatusNot(Integer companyId, WorkspaceStatus status);

    // ======================================================
    // 3. TRUY VẤN DỮ LIỆU (RETRIEVAL)
    // ======================================================

    /**
     * Lấy danh sách toàn bộ ID Workspace thuộc về một Công ty.
     * Dùng cho các truy vấn kiểm tra quyền hạn hoặc lọc phạm vi dữ liệu diện rộng.
     */
    @Query(FIND_IDS_BY_COMPANY_QUERY)
    List<Integer> findWorkspaceIdsByCompanyId(@Param("companyId") Integer companyId);

    /**
     * Lấy danh sách Không gian làm việc của một công ty hỗ trợ phân trang và sắp xếp.
     */
    Page<Workspace> findByCompany_Id(Integer companyId, Pageable pageable);
}