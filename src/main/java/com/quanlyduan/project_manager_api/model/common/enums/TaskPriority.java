// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/TaskPriority.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Mức độ ưu tiên của Task (công việc).
 * Enum này khớp với CSDL: priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT').
 */
public enum TaskPriority {
    LOW,     // Thấp
    MEDIUM,  // Trung bình
    HIGH,    // Cao
    URGENT   // Khẩn cấp
}