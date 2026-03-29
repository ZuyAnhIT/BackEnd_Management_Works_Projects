package com.quanlyduan.project_manager_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.Tag;

/**
 * Kho lưu trữ dữ liệu quản lý các nhãn (Tags) của công việc.
 * Hỗ trợ các thao tác kiểm tra tính duy nhất và tìm kiếm động thông qua JpaSpecificationExecutor.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Integer>, JpaSpecificationExecutor<Tag> {

    // ======================================================
    // 1. KIỂM TRA RÀNG BUỘC (VALIDATION)
    // ======================================================

    /**
     * Kiểm tra sự tồn tại của nhãn theo tên trong phạm vi một Dự án.
     * Sử dụng để ngăn chặn việc tạo trùng lặp tên nhãn khi tạo mới.
     * @param name Tên nhãn cần kiểm tra.
     * @param projectId ID của dự án chứa nhãn.
     * @return true nếu tên nhãn đã tồn tại trong dự án.
     */
    boolean existsByNameAndProject_Id(String name, Integer projectId);

    /**
     * Kiểm tra sự tồn tại của tên nhãn trong dự án, loại trừ nhãn hiện tại.
     * Sử dụng để đảm bảo tính duy nhất của tên nhãn khi thực hiện cập nhật.
     * @param name Tên nhãn mới.
     * @param projectId ID của dự án.
     * @param id ID của nhãn hiện tại (để loại trừ khỏi quá trình kiểm tra).
     * @return true nếu tên nhãn bị trùng với một nhãn khác trong dự án.
     */
    boolean existsByNameAndProject_IdAndIdNot(String name, Integer projectId, Integer id);
}