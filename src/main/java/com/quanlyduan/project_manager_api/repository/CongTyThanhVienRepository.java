package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CongTyThanhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CongTyThanhVienRepository extends JpaRepository<CongTyThanhVien, Integer> {
}