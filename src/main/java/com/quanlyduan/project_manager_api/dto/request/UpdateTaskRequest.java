package com.quanlyduan.project_manager_api.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat thong tin Cong viec (Task).
 * Ho tro co che Partial Update: Chi nhung truong khong null moi duoc cap nhat vao Database.
 */
@Getter
@Setter
public class UpdateTaskRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int BACKLOG_SPRINT_ID = 0;
    public static final int UNASSIGNED_EPIC_ID = 0;
    public static final int NAME_MAX_SIZE = 255;
    
    public static final String NAME_SIZE_MSG = "Task title must not exceed " + NAME_MAX_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN CO BAN (BASIC INFO)
    // ======================================================

    /**
     * Tieu de moi cua Task.
     */
    @Size(max = NAME_MAX_SIZE, message = NAME_SIZE_MSG)
    private String title;

    /**
     * Mo ta chi tiet moi ve noi dung cong viec.
     */
    private String description;

    // ======================================================
    // 2. PHAN LOAI & TRANG THAI (CLASSIFICATION)
    // ======================================================

    /**
     * Loai cong viec moi (TASK, BUG, STORY, v.v.).
     */
    private TaskType taskType;

    /**
     * Muc do uu tien moi (LOW, MEDIUM, HIGH, URGENT).
     */
    private TaskPriority priority;
    
    /**
     * ID cua Trang thai/Cot moi tren Board.
     * Su dung de di chuyen Task giua cac cot (vi du: tu To Do sang Doing).
     */
    private Integer statusId; 

    // ======================================================
    // 3. LIEN KET & QUAN HE (RELATIONSHIPS)
    // ======================================================
    
    /**
     * ID cua Sprint.
     * - Null: Giu nguyen.
     * - 0 (BACKLOG_SPRINT_ID): Dua Task ve Backlog.
     * - > 0: Chuyen sang Sprint tuong ung.
     */
    private Integer sprintId; 

    /**
     * ID cua Epic lien quan.
     * Logic tuong tu SprintId de thuc hien gan moi hoac go bo khoi Epic.
     */
    private Integer epicId;

    /**
     * ID cua nguoi dung duoc giao thuc hien cong viec (Assignee).
     */
    private Integer assigneeId; 
    
    // ======================================================
    // 4. UOC LUONG & THOI GIAN (PLANNING)
    // ======================================================

    /**
     * Diem cau chuyen (Story Points) dung trong Scrum.
     */
    private Integer storyPoints;

    /**
     * Thoi gian uoc tinh thuc hien (don vi: gio).
     */
    private BigDecimal estimatedHours;
    
    /**
     * Thoi diem bat dau du kien.
     */
    private LocalDateTime startDate;

    /**
     * Han chot phai hoan thanh (Deadline).
     */
    private LocalDateTime dueDate;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public UpdateTaskRequest() {
    }
}