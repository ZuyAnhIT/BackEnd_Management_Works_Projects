package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.WorkSpace;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkSpaceRepository extends JpaRepository<WorkSpace, Integer> {
    
    // Kiểm tra tên không gian đã tồn tại trong công ty chưa
    boolean existsByCongTy_IdCongTyAndTenKhongGian(Integer congTyId, String tenKhongGian);

    // *** THÊM PHƯƠNG THỨC NÀY ***
    /**
     * Tìm tất cả các không gian làm việc theo ID của công ty.
     * @param congTyId ID của công ty
     * @return Danh sách các KhongGian
     */
    List<WorkSpace> findByCongTy_IdCongTy(Integer congTyId);

    
}