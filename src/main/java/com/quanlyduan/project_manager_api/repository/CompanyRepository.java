// File: src/main/java/com/quanlyduan/project_manager_api/repository/CompanyRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Company; // Đã dịch
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> { // Đã dịch
    Boolean existsByName(String name); // Đã dịch
}