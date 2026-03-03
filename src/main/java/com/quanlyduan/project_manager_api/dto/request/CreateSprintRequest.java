package com.quanlyduan.project_manager_api.dto.request;

// Java Utils
import java.time.LocalDateTime;
import java.util.List;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới một Sprint.
 * Hỗ trợ cơ chế "Tạo nhanh" (Quick Create), do đó tất cả các trường đều là tùy chọn (Optional).
 */
@Data
public class CreateSprintRequest {

    // ==========================================
    // REQUEST DATA (Thông tin Sprint)
    // ==========================================

    /**
     * Tên của Sprint.
     * (Tùy chọn) Nếu null hoặc rỗng, hệ thống sẽ tự động sinh tên theo thứ tự (ví dụ: "Sprint 1", "Sprint 2").
     */
    private String name;

    /**
     * Mục tiêu của Sprint (Sprint Goal).
     * Giúp team tập trung vào giá trị cốt lõi cần đạt được trong chu kỳ này.
     * (Tùy chọn)
     */
    private String goal;

    /**
     * Thời gian bắt đầu dự kiến của Sprint.
     * Định dạng chuẩn ISO: YYYY-MM-DDTHH:mm:ss
     * (Tùy chọn)
     */
    private LocalDateTime startDate;

    /**
     * Thời gian kết thúc dự kiến của Sprint.
     * (Tùy chọn) Thường được tính toán tự động dựa trên cấu hình độ dài Sprint (ví dụ: 2 tuần) nếu người dùng không nhập.
     */
    private LocalDateTime endDate;

    /**
     * Danh sách ID của các Task muốn thêm ngay vào Sprint này khi vừa khởi tạo.
     * (Tùy chọn) Hỗ trợ tốt cho thao tác kéo thả hoặc chọn nhiều (Bulk Select) từ Backlog.
     */
    private List<Integer> taskIds;

}