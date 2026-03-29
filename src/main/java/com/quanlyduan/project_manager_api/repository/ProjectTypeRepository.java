package com.quanlyduan.project_manager_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.ProjectType;

/**
 * Kho lưu trữ dữ liệu quản lý các Loại Dự án (Project Types).
 * Cung cấp các thao tác CRUD cơ bản để hỗ trợ việc phân loại và cấu hình dự án.
 */
@Repository
public interface ProjectTypeRepository extends JpaRepository<ProjectType, Integer> {
    
    // Interface này hiện tại sử dụng toàn bộ các phương thức mặc định của JpaRepository 
    // như: findById, findAll, save, delete...
    
}