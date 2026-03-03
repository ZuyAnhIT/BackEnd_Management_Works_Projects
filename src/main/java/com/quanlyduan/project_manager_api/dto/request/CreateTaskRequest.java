package com.quanlyduan.project_manager_api.dto.request;

// Enums
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

// Validation
import jakarta.validation.constraints.NotBlank;

// Java Utils
import java.time.LocalDateTime;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới một Công việc (Task).
 * Thiết kế linh hoạt để hỗ trợ cả cơ chế "Tạo nhanh" (chỉ cần Title) và "Tạo đầy đủ".
 */
@Data
public class CreateTaskRequest {

    // ==========================================
    // REQUIRED DATA (Thông tin bắt buộc)
    // ==========================================

    /**
     * Tiêu đề công việc.
     * Bắt buộc phải có, không được để trống.
     */
    @NotBlank(message = "Task title must not be blank") 
    private String title;

    // ==========================================
    // OPTIONAL DATA (Thông tin tùy chọn cơ bản)
    // ==========================================

    /**
     * Mô tả chi tiết nội dung công việc.
     * (Tùy chọn)
     */
    private String description;

    /**
     * Phân loại công việc (Ví dụ: TASK, BUG, STORY).
     * (Tùy chọn) Nếu để null, tầng Service sẽ tự động gán giá trị mặc định (Ví dụ: TASK).
     */
    private TaskType taskType; 

    /**
     * Mức độ ưu tiên của công việc.
     * (Tùy chọn) Nếu để null, tầng Service sẽ tự động gán giá trị mặc định (Ví dụ: MEDIUM).
     */
    private TaskPriority priority; 

    // ==========================================
    // RELATIONSHIPS & METRICS (Liên kết và đo lường)
    // ==========================================

    /**
     * ID của Sprint chứa Task này.
     * (Tùy chọn)
     * - Nếu có giá trị: Task được thêm trực tiếp vào Sprint đó.
     * - Nếu là Null: Task sẽ được đẩy vào danh sách chờ (Backlog) của dự án.
     */
    private Integer sprintId; 

    /**
     * ID của Epic chứa Task này.
     * (Tùy chọn) Dùng để gom nhóm các Task vào một tính năng/mục tiêu lớn hơn.
     */
    private Integer epicId;      

    /**
     * ID của người được giao thực hiện công việc (Assignee).
     * (Tùy chọn)
     */
    private Integer assigneeId;  

    /**
     * Điểm ước lượng độ phức tạp hoặc khối lượng công việc (Story Points).
     * (Tùy chọn) Được sử dụng phổ biến trong quy trình Scrum để tính toán Velocity.
     */
    private Integer storyPoints; 

    /**
     * Hạn chót hoàn thành công việc (Due Date).
     * (Tùy chọn)
     */
    private LocalDateTime dueDate; 

}