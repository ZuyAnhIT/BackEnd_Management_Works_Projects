package com.quanlyduan.project_manager_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.SubTask;

/**
 * Kho lưu trữ dữ liệu quản lý các công việc con (Sub-tasks).
 * Hỗ trợ điều khiển thứ tự hiển thị và các thao tác cập nhật vị trí trong danh sách công việc.
 */
@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Integer> {

    // ======================================================
    // 1. TRUY VẤN DỮ LIỆU (RETRIEVAL)
    // ======================================================

    /**
     * Lấy toàn bộ danh sách công việc con của một công việc cha.
     * Kết quả được sắp xếp theo thứ tự hiển thị tăng dần để hiển thị trên giao diện chi tiết.
     * @param taskId ID của công việc cha.
     */
    List<SubTask> findByParentTask_IdOrderBySortOrderAsc(Integer taskId);

    // ======================================================
    // 2. LOGIC CẬP NHẬT VỊ TRÍ (SHIFTING & SORTING)
    // ======================================================

    /**
     * Tìm các công việc con có thứ tự sắp xếp lớn hơn một giá trị cụ thể trong cùng công việc cha.
     * Thường dùng để dịch chuyển vị trí (Shifting) của các phần tử phía sau khi thực hiện kéo thả hoặc xóa.
     * @param taskId ID của công việc cha.
     * @param sortOrder Giá trị thứ tự mốc để so sánh.
     */
    List<SubTask> findByParentTask_IdAndSortOrderGreaterThanOrderBySortOrderAsc(
            Integer taskId,
            Integer sortOrder
    );

    // ======================================================
    // 3. TIỆN ÍCH HỖ TRỢ (UTILITIES)
    // ======================================================

    /**
     * Đếm tổng số lượng công việc con thuộc về một công việc cha.
     * Dùng để xác định giá trị 'sort_order' tiếp theo khi tạo mới một công việc con.
     * @param taskId ID của công việc cha.
     * @return Tổng số lượng công việc con hiện có.
     */
    Integer countByParentTask_Id(Integer taskId);

}