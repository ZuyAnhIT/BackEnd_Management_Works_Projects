package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi danh sach cong viec ca nhan cua nguoi dung (My Tasks).
 * Cung cap day du thong tin ve trang thai, do uu tien va ngu canh Du an/Workspace de hien thi tren Dashboard.
 */
@Getter
@Setter
@Builder
public class MyTaskResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (TASK IDENTITY)
    // ======================================================
    
    /** ID duy nhat cua Task trong CSDL. */
    private Integer taskId;
    
    /** Ma code cua Task (vi du: "PROJ-101"). */
    private String taskCode;
    
    /** Tieu de chinh cua cong viec. */
    private String taskTitle;

    // ======================================================
    // 2. TRANG THAI & DO UU TIEN (STATUS & PRIORITY)
    // ======================================================

    /** ID, Ten va Mau sac dai dien cho trang thai (vi du: 1, "Doing", "#3498db"). */
    private Integer taskStatusId; 
    private String taskStatusName; 
    private String taskStatusColor; 

    /** Do uu tien cua cong viec (LOW, MEDIUM, HIGH, URGENT). */
    private TaskPriority taskPriority;

    // ======================================================
    // 3. THOI HAN (TIMELINE)
    // ======================================================

    /** Han chot de hoan thanh cong viec. */
    private LocalDateTime taskDueDate;

    // ======================================================
    // 4. NGU CANH PHAN CAP (HIERARCHY CONTEXT)
    // ======================================================
    // Giup nguoi dung biet Task nay thuoc ve Du an va Phong ban nao.

    /** Thong tin Du an (Project) chua Task nay. */
    private Integer projectId;
    private String projectName;

    /** Thong tin Khong gian lam viec (Workspace/Department) chua Project nay. */
    private Integer workspaceId;
    private String workspaceName;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Giup Jackson Deserialize du lieu mot cach minh bach.
     */
    public MyTaskResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public MyTaskResponse(Integer taskId, String taskCode, String taskTitle, 
                          Integer taskStatusId, String taskStatusName, String taskStatusColor, 
                          TaskPriority taskPriority, LocalDateTime taskDueDate, 
                          Integer projectId, String projectName, 
                          Integer workspaceId, String workspaceName) {
        this.taskId = taskId;
        this.taskCode = taskCode;
        this.taskTitle = taskTitle;
        this.taskStatusId = taskStatusId;
        this.taskStatusName = taskStatusName;
        this.taskStatusColor = taskStatusColor;
        this.taskPriority = taskPriority;
        this.taskDueDate = taskDueDate;
        this.projectId = projectId;
        this.projectName = projectName;
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
    }
}