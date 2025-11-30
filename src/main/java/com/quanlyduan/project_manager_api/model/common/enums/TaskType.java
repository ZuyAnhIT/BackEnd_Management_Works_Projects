// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/TaskType.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Các loại công việc (issue types) khác nhau trong quản lý dự án (Agile/Scrum).
 */
public enum TaskType {
    STORY,   // Yêu cầu tính năng từ góc nhìn người dùng (User Story)
    TASK,    // Công việc kỹ thuật/triển khai nhỏ
    BUG,     // Lỗi hoặc vấn đề cần sửa chữa
    EPIC,    // Nhóm các Story lớn (cần quản lý ở cấp độ cao hơn, mặc dù TaskType có thể chứa nó,
             // nhưng thường Epic là Entity cha riêng biệt)
    SUBTASK  // Công việc con của một Task hoặc Story
}