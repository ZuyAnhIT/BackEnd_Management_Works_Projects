package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer>, JpaSpecificationExecutor<Tag> {
    boolean existsByNameAndProject_Id(String name, Integer projectId);
    /**
     * Kiểm tra xem có tag nào KHÁC (khác ID này) trong cùng Project có trùng tên không.
     */
    boolean existsByNameAndProject_IdAndIdNot(String name, Integer projectId, Integer id);
}
