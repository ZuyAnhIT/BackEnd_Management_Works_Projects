// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/TaskFilterRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * Đối tượng chứa các tham số để lọc (filter) danh sách Task.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskFilterRequest {

    // Lọc theo Project (Gần như luôn bắt buộc)
    private Integer projectId;

    // Lọc theo Epic
    private Integer epicId;
    
    // Lọc theo Sprint
    private Integer sprintId;

    // Tìm kiếm theo tiêu đề (thường là tìm kiếm 'like')
    private String title;

    // Lọc theo một hoặc nhiều loại Task
    private Set<TaskType> taskTypes;

    // Lọc theo trạng thái
    private String status;

    // Lọc theo một hoặc nhiều mức độ ưu tiên
    private Set<Priority> priorities;

    // Lọc theo người được gán
    private Integer assigneeId;

    // Lọc theo người review
    private Integer reviewerId;

    // Lọc theo ngày bắt đầu (trong khoảng)
    private LocalDate startDateFrom;
    private LocalDate startDateTo;

    // Lọc theo ngày hết hạn (trong khoảng)
    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;
}