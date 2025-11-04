package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.KhongGian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhongGianRepository extends JpaRepository<KhongGian, Integer> {
    
    // Kiểm tra tên không gian đã tồn tại trong công ty chưa
    boolean existsByCongTy_IdCongTyAndTenKhongGian(Integer congTyId, String tenKhongGian);
}