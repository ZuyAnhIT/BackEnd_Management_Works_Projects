// File: src/main/java/com/quanlyduan/project_manager_api/model/common/enums/SprintStatus.java
package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Trạng thái của một Sprint trong quy trình Scrum/Agile.
 * Maps to the ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') in sprints table.
 */
public enum SprintStatus {
    NOT_STARTED, // Chưa bắt đầu, đang ở trạng thái chuẩn bị (Planned)
    IN_PROGRESS, // Đang diễn ra, công việc đang được thực hiện (Active)
    COMPLETED,   // Đã hoàn thành (Done)
    CANCELLED    // Đã bị hủy bỏ trước khi hoàn thành
}