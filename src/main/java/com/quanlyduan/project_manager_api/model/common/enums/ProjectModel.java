// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/ProjectModel.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Các mô hình quản lý dự án chính.
 * Khớp với CSDL: model ENUM('SCRUM', 'KANBAN', 'WATERFALL', 'HYBRID')
 */
public enum ProjectModel {
    SCRUM,       // Mô hình phát triển lặp đi lặp lại (Agile)
    KANBAN,      // Mô hình quản lý công việc trực quan (Agile)
    WATERFALL,   // Mô hình tuần tự, tuyến tính (truyền thống)
    HYBRID       // Mô hình kết hợp giữa Agile và Truyền thống
}