package com.quanlyduan.project_manager_api.dto.request;

// Enums
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

// Java Utils
import java.util.List;

// Lombok
import lombok.Data;

/**
 * DTO chứa thông tin yêu cầu gợi ý người thực hiện (Assignee) phù hợp nhất cho một công việc.
 * Thường được sử dụng để làm dữ liệu đầu vào cho các tính năng AI/Smart Recommendation.
 */
@Data
public class AssigneeRecommendationRequest {

    // ==========================================
    // REQUEST DATA (Thông tin công việc)
    // ==========================================

    /**
     * Tiêu đề công việc.
     * VD: "Fix lỗi thanh toán VNPAY"
     */
    private String title;

    /**
     * Mô tả chi tiết nội dung hoặc lỗi gặp phải.
     * VD: "API trả về lỗi 500 khi callback từ VNPAY..."
     */
    private String description;

    /**
     * Loại công việc cần xử lý.
     * VD: BUG, TASK, STORY...
     */
    private TaskType taskType;

    /**
     * Danh sách các thẻ (tags) liên quan đến chuyên môn hoặc module hệ thống.
     * VD: ["Backend", "Payment", "Java", "Spring Boot"]
     */
    private List<String> tags;

    /**
     * Điểm ước lượng độ phức tạp của công việc (Story Points).
     * Dữ liệu này giúp thuật toán tính toán tải công việc hiện tại của nhân sự, tránh tình trạng giao việc quá tải (overload).
     * VD: 5
     */
    private Integer storyPoints;

}