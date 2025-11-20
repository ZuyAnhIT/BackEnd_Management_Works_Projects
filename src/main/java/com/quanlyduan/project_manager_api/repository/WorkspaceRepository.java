// File: src/main/java/com/quanlyduan/project_manager_api/repository/WorkspaceRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Workspace; // Đã dịch

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer> { // Đã dịch
    
    // Kiểm tra tên không gian đã tồn tại trong công ty chưa
    boolean existsByCompany_IdAndName(Integer companyId, String workspaceName); // Đã dịch

    
    /**
     * Lấy danh sách không gian làm việc theo ID công ty (Có phân trang).
     * Spring Data JPA tự động xử lý LIMIT/OFFSET.
     */
    Page<Workspace> findByCompany_Id(Integer companyId, Pageable pageable);

    /**
     * Tìm workspace theo Tên và ID Công ty.
     * Dùng để kiểm tra tên trùng lặp khi CẬP NHẬT.
     */
    Optional<Workspace> findByCompany_IdAndName(Integer companyId, String name);

    
    @Query("SELECT w.id FROM Workspace w WHERE w.company.id = :companyId")
    List<Integer> findWorkspaceIdsByCompanyId(@Param("companyId") Integer companyId);

    
    
}