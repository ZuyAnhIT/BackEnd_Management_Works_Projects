// File: src/main/java/com/quanlyduan/project_manager_api/repository/WorkspaceRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Workspace; // Đã dịch

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer> { // Đã dịch
    
    // Kiểm tra tên không gian đã tồn tại trong công ty chưa
    boolean existsByCompany_IdAndName(Integer companyId, String workspaceName); // Đã dịch

    // *** THÊM PHƯƠNG THỨC NÀY ***
    /**
     * Tìm tất cả các không gian làm việc theo ID của công ty.
     * @param congTyId ID của công ty
     * @return Danh sách các KhongGian
     */
    List<Workspace> findByCompany_Id(Integer companyId); // Đã dịch
    /**
     * Tìm workspace theo Tên và ID Công ty.
     * Dùng để kiểm tra tên trùng lặp khi CẬP NHẬT.
     */
    Optional<Workspace> findByCompany_IdAndName(Integer companyId, String name);

    
    @Query("SELECT w.id FROM Workspace w WHERE w.company.id = :companyId")
    List<Integer> findWorkspaceIdsByCompanyId(@Param("companyId") Integer companyId);

    
    
}