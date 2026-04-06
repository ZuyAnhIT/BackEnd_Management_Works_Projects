package com.quanlyduan.project_manager_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    String COUNT_NEW_COMPANIES_BY_DATE_RANGE = 
        "SELECT COUNT(c) FROM Company c " +
        "WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate";

    /**
     * Dem so luong cong ty dang ky moi trong mot khoang thoi gian.
     */
    @Query(COUNT_NEW_COMPANIES_BY_DATE_RANGE)
    long countNewCompaniesByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                      @Param("endDate") java.time.LocalDateTime endDate);
    
    String SUM_ALL_STORAGE_USED = "SELECT COALESCE(SUM(c.currentStorageBytes), 0) FROM Company c";

    /**
     * Calculate the total storage space consumed by all companies.
     */
    @Query(SUM_ALL_STORAGE_USED)
    long sumTotalStorageUsed();


}