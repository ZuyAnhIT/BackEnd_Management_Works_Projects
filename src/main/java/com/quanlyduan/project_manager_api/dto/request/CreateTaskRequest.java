package com.quanlyduan.project_manager_api.dto.request;

import java.time.LocalDateTime;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu Client de tao moi mot Cong viec (Task).
 * Thiet ke linh hoat de ho tro ca co che Tao nhanh (chi can Tieu de) va Tao day du.
 */
@Getter
@Setter
public class CreateTaskRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String TITLE_BLANK_MSG = "Task title must not be blank";

    // ======================================================
    // 1. THONG TIN BAT BUOC (REQUIRED)
    // ======================================================

    /**
     * Tieu de cua cong viec.
     * Bat buoc phai co de dinh danh nhiem vu trong he thong.
     */
    @NotBlank(message = TITLE_BLANK_MSG) 
    private String title;

    // ======================================================
    // 2. THONG TIN CHI TIET (DETAILS)
    // ======================================================

    /**
     * Mo ta chi tiet noi dung hoac cach thuc thuc hien cong viec.
     */
    private String description;

    /**
     * Phan loai cong viec (vi du: TASK, BUG, STORY).
     * Neu de null, tang Service se tu dong gan gia tri mac dinh la TASK.
     */
    private TaskType taskType; 

    /**
     * Muc do uu tien cua cong viec doi voi du an.
     * Neu de null, tang Service se tu dong gan gia tri mac dinh la MEDIUM.
     */
    private TaskPriority priority; 

    // ======================================================
    // 3. QUAN HE LIEN KET (RELATIONSHIPS)
    // ======================================================

    /**
     * ID cua Sprint chua cong viec nay.
     * Neu null, Task se duoc dua vao danh sach cho (Backlog).
     */
    private Integer sprintId; 

    /**
     * ID cua Epic chua Task nay nham nhom vao mot tinh nang lon hon.
     */
    private Integer epicId;      

    /**
     * ID cua nguoi duoc phan cong thuc hien (Assignee).
     */
    private Integer assigneeId;  

    // ======================================================
    // 4. CHI SO VA THOI HAN (METRICS & DEADLINE)
    // ======================================================

    /**
     * Diem uoc luong do phuc tap cua cong viec (Story Points).
     * Dung de tinh toan nang suat (Velocity) trong quy trinh Agile.
     */
    private Integer storyPoints; 

    /**
     * Han chot phai hoan thanh cong viec (Due Date).
     */
    private LocalDateTime dueDate; 

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu chuoi JSON.
     */
    public CreateTaskRequest() {
    }
}