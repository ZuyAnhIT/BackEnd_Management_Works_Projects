package com.quanlyduan.project_manager_api.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi cau truc cua mot Cot (Column/Status) tren bang Kanban hoac Scrum.
 * Chua thong tin dinh nghia trang thai va danh sach cac Card (Task) tuong ung.
 */
@Getter
@Setter
@Builder
public class BoardColumnResponse {

    // ======================================================
    // 1. DINH NGHIA TRANG THAI (STATUS DEFINITION)
    // ======================================================

    /** ID dinh danh cua trang thai (ProjectStatus ID). */
    private Integer statusId;

    /** Ten hien thi cua cot (vi du: "To Do", "In Progress", "Done"). */
    private String statusName;

    /** Ma mau HEX dai dien cho cot de dong bo UI (vi du: "#3498db"). */
    private String color;

    // ======================================================
    // 2. CAU HINH HIEN THI & LOGIC (UI & LOGIC CONFIG)
    // ======================================================

    /** Thu tu sap xep cua cot tren giao dien (tinh tu trai qua phai). */
    private Integer order;

    /** * Co danh dau cot "Hoan thanh". 
     * Neu true, cac task keo vao day se duoc he thong tinh toan la da xong. 
     */
    private Boolean isCompleted;

    // ======================================================
    // 3. DANH SACH CONG VIEC (TASK LIST)
    // ======================================================

    /** * Danh sach cac thẻ cong viec dang nam trong cot nay.
     * Su dung TaskSummaryResponse de hien thi day du thong tin: Assignee, Tags, Epic... 
     */
    private List<TaskSummaryResponse> tasks;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay de Jackson thuc hien Deserialize.
     */
    public BoardColumnResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public BoardColumnResponse(Integer statusId, String statusName, String color, 
                               Integer order, Boolean isCompleted, 
                               List<TaskSummaryResponse> tasks) {
        this.statusId = statusId;
        this.statusName = statusName;
        this.color = color;
        this.order = order;
        this.isCompleted = isCompleted;
        this.tasks = tasks;
    }
}