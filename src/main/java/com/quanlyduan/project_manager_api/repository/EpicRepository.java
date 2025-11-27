// File: src/main/java/com.quanlyduan.project_manager_api/repository/EpicRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Epic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Integer>, JpaSpecificationExecutor<Epic> { 
    
    // Lấy danh sách Epic của dự án
    List<Epic> findByProject_Id(Integer projectId);
    
    // Đếm số lượng để sinh mã Epic tự động
    long countByProject_Id(Integer projectId);
    
}