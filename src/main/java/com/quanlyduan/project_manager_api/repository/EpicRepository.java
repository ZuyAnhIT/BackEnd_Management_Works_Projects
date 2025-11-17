// File: src/main/java/com/quanlyduan/project_manager_api/repository/EpicRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Epic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Integer> {
    
    /**
     * Tìm Epic bằng ID và đảm bảo nó thuộc đúng Project
     */
    Optional<Epic> findByIdAndProject_Id(Integer epicId, Integer projectId);
}