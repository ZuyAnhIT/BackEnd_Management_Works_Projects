// File: src/main/java/com/quanlyduan/project_manager_api/repository/CompanyRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Company; // Entity Công ty
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repository cho Entity Company (Quản lý các thao tác với bảng companies).
 */
public interface CompanyRepository extends JpaRepository<Company, Integer>, JpaSpecificationExecutor<Company> {

    /**
     * Kiểm tra xem đã có Công ty nào tồn tại với tên này chưa.
     */
    Boolean existsByName(String name);
}