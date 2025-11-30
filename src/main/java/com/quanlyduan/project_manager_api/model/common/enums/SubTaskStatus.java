// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/SubTaskStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của một Subtask (công việc con) thuộc Task.
 */
public enum SubTaskStatus {
    TO_DO,       // Cần làm (Chưa bắt đầu)
    IN_PROGRESS, // Đang thực hiện
    DONE         // Đã hoàn thành
}