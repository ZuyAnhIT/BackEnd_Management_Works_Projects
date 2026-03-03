package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Các loại công việc (Issue Types) khác nhau trong quản lý dự án (Agile/Scrum).
 * Phân loại giúp xác định quy trình xử lý và hiển thị biểu tượng tương ứng trên UI.
 */
public enum TaskType {

    // ==========================================
    // ENUM VALUES (Giá trị loại công việc)
    // ==========================================

    /** Yêu cầu tính năng từ góc nhìn người dùng (User Story) */
    STORY,

    /** Công việc kỹ thuật hoặc các đầu mục triển khai nhỏ */
    TASK,

    /** Lỗi phát sinh hoặc các vấn đề cần sửa chữa (Defect) */
    BUG,

    /** * Nhóm các Story lớn. 
     * Lưu ý: Thường được quản lý bởi Entity Epic riêng biệt, 
     * nhưng có thể dùng làm type để hiển thị đồng nhất. 
     */
    EPIC,

    /** Công việc con cấp nhỏ nhất của một Task hoặc Story */
    SUBTASK

}