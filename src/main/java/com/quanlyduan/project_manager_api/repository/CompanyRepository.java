package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
    Boolean existsByTenCongTy(String tenCongTy);
}