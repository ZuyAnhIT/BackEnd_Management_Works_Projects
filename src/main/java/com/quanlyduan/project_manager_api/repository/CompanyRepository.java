package com.quanlyduan.project_manager_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.Company;

/**
 * Kho lưu trữ dữ liệu cho thực thể Công ty (Company).
 * Hỗ trợ các thao tác CRUD cơ bản và tìm kiếm động thông qua JpaSpecificationExecutor.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer>, JpaSpecificationExecutor<Company> {

    /**
     * Kiểm tra sự tồn tại của công ty dựa trên tên đăng ký.
     * @param name Tên công ty cần kiểm tra.
     * @return true nếu tên công ty đã tồn tại trong hệ thống.
     */
    boolean existsByName(String name);
}