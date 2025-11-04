package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.KhongGianThanhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhongGianThanhVienRepository extends JpaRepository<KhongGianThanhVien, Integer> {
}