package com.quanlyduan.project_manager_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;

/**
 * Kho lưu trữ dữ liệu quản lý các chu kỳ phát triển (Sprint).
 * Hỗ trợ các thao tác điều khiển vòng đời Sprint trong mô hình Agile/Scrum.
 */
@Repository
public interface SprintRepository extends JpaRepository<Sprint, Integer>, JpaSpecificationExecutor<Sprint> {

    // Khai báo các câu truy vấn tĩnh để tránh hardcode trong mã nguồn
    String FIND_ACTIVE_SPRINTS_QUERY = "SELECT s FROM Sprint s WHERE s.project.id = :projectId " +
                                       "AND s.status IN :statuses ORDER BY s.startDate ASC";

    String FIND_IN_PROGRESS_SPRINT_QUERY = "SELECT s FROM Sprint s WHERE s.project.id = :projectId " +
                                           "AND s.status = 'IN_PROGRESS'";

    // ======================================================
    // 1. TRUY VẤN TRẠNG THÁI HIỆN TẠI (ACTIVE STATUS)
    // ======================================================

    /**
     * Tìm Sprint đang được thực hiện (IN_PROGRESS) của một dự án.
     * Mỗi dự án tại một thời điểm thường chỉ có tối đa một Sprint đang chạy.
     */
    @Query(FIND_IN_PROGRESS_SPRINT_QUERY)
    Optional<Sprint> findActiveSprintByProjectId(@Param("projectId") Integer projectId);

    /**
     * Lấy danh sách các Sprint đang ở trạng thái hoạt động (ví dụ: NOT_STARTED, IN_PROGRESS).
     * Phục vụ hiển thị danh sách Sprint sắp tới hoặc đang chạy trên Backlog.
     */
    @Query(FIND_ACTIVE_SPRINTS_QUERY)
    List<Sprint> findActiveSprintsByProjectId(@Param("projectId") Integer projectId,
                                              @Param("statuses") List<SprintStatus> statuses);

    /**
     * Kiểm tra xem trong dự án có Sprint nào đang ở một trạng thái cụ thể hay không.
     * Dùng để ngăn chặn việc bắt đầu nhiều Sprint cùng lúc.
     */
    boolean existsByProject_IdAndStatus(Integer projectId, SprintStatus status);

    // ======================================================
    // 2. TRUY VẤN DỮ LIỆU CƠ BẢN (RETRIEVAL)
    // ======================================================

    /**
     * Tìm kiếm Sprint theo ID và đảm bảo thuộc đúng dự án cung cấp.
     */
    Optional<Sprint> findByIdAndProject_Id(Integer sprintId, Integer projectId);

    /**
     * Lấy toàn bộ danh sách Sprint của dự án, ưu tiên những Sprint mới nhất lên đầu.
     */
    List<Sprint> findByProject_IdOrderByStartDateDesc(Integer projectId);

    /**
     * Lọc danh sách Sprint theo dự án và trạng thái cụ thể, sắp xếp theo ngày bắt đầu.
     */
    List<Sprint> findByProject_IdAndStatusOrderByStartDateDesc(Integer projectId, SprintStatus status);

    // ======================================================
    // 3. THỐNG KÊ VÀ PHÂN TÍCH (ANALYTICS & UTILITIES)
    // ======================================================

    /**
     * Đếm tổng số lượng Sprint trong dự án.
     * Dùng để tự động sinh tên Sprint (Ví dụ: Sprint 1, Sprint 2...).
     */
    long countByProject_Id(Integer projectId);

    /**
     * Lấy danh sách 5 Sprint gần nhất đã hoàn thành.
     * Phục vụ cho việc tính toán vận tốc trung bình (Velocity) của đội ngũ.
     */
    List<Sprint> findTop5ByProject_IdAndStatusOrderByEndDateDesc(Integer projectId, SprintStatus status);
}