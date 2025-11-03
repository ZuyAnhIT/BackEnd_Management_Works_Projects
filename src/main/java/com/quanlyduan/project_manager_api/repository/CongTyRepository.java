package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CongTy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CongTyRepository extends JpaRepository<CongTy, Integer> {
    Boolean existsByTenCongTy(String tenCongTy);
}