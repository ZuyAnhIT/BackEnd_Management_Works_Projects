// File: src/main/java/com/quanlyduan/project_manager_api/repository/EpicRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Epic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Integer>, JpaSpecificationExecutor<Epic> {
    
    List<Epic> findByProject_Id(Integer projectId);
    
    long countByProject_Id(Integer projectId);
    
    // 1. Kiểm tra khi Tạo mới (Trong cùng 1 dự án, không được có tên trùng)
    boolean existsByProject_IdAndNameIgnoreCase(Integer projectId, String name);

    // 2. Kiểm tra khi Cập nhật (Trùng tên nhưng khác ID của chính nó)
    boolean existsByProject_IdAndNameIgnoreCaseAndIdNot(Integer projectId, String name, Integer id);
}