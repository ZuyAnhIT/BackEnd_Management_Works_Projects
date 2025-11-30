// File: src/main/java/com/quanlyduan/project_manager_api/repository/WorkspaceRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Workspace; // Entity Workspace

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
/**
 * Repository cho Entity Workspace (Quản lý các không gian làm việc).
 * Kế thừa JpaSpecificationExecutor để hỗ trợ tìm kiếm động.
 */
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer>, JpaSpecificationExecutor<Workspace> {

    /**
     * Kiểm tra xem tên Workspace đã tồn tại trong phạm vi Công ty chưa.
     * Dùng cho logic tạo mới.
     */
    boolean existsByCompany_IdAndName(Integer companyId, String workspaceName);

    /**
     * Lấy danh sách không gian làm việc theo ID công ty (Có phân trang).
     */
    Page<Workspace> findByCompany_Id(Integer companyId, Pageable pageable);

    /**
     * Tìm Workspace theo Tên và ID Công ty.
     * Dùng để kiểm tra tên trùng lặp khi CẬP NHẬT.
     */
    Optional<Workspace> findByCompany_IdAndName(Integer companyId, String name);

    /**
     * Lấy danh sách tất cả các ID Workspace thuộc về một Công ty.
     * Dùng cho các truy vấn kiểm tra quyền/phạm vi.
     */
    @Query("SELECT w.id FROM Workspace w WHERE w.company.id = :companyId")
    List<Integer> findWorkspaceIdsByCompanyId(@Param("companyId") Integer companyId);
}