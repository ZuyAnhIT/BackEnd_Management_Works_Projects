package com.quanlyduan.project_manager_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.TaskAttachment;

/**
 * Kho lưu trữ dữ liệu quản lý các tệp đính kèm của công việc (Task Attachments).
 * Cung cấp các phương thức truy xuất tài liệu, hình ảnh liên quan đến từng nhiệm vụ cụ thể.
 */
@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Integer> {

    // ======================================================
    // 1. TRUY VẤN DỮ LIỆU (RETRIEVAL)
    // ======================================================

    /**
     * Lấy danh sách tất cả các tệp đính kèm thuộc về một công việc.
     * @param taskId ID của công việc cần truy vấn tệp tin.
     * @return Danh sách các tệp đính kèm tìm thấy.
     */
    List<TaskAttachment> findByTask_Id(Integer taskId);
}