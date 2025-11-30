// File: src/main/java/com/quanlyduan/project_manager_api/repository/TagRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Tag; // Entity Tag
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repository cho Entity Tag (Quản lý các nhãn/tags được gán cho Task).
 * Kế thừa JpaSpecificationExecutor để hỗ trợ tìm kiếm động.
 */
public interface TagRepository extends JpaRepository<Tag, Integer>, JpaSpecificationExecutor<Tag> {

    /**
     * Kiểm tra xem đã tồn tại Tag với Tên (chính xác) trong Dự án chưa.
     * Dùng cho việc tạo Tag mới.
     */
    boolean existsByNameAndProject_Id(String name, Integer projectId);

    /**
     * Kiểm tra tính duy nhất của Tên Tag trong phạm vi một Dự án,
     * loại trừ Tag đang được cập nhật (khác ID).
     * Dùng cho việc cập nhật Tag.
     */
    boolean existsByNameAndProject_IdAndIdNot(String name, Integer projectId, Integer id);
}