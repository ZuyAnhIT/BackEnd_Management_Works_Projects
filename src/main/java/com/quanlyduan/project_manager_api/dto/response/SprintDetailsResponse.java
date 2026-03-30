package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet cua mot Sprint (Vong lap phat trien).
 * Bao gom ho so Sprint, thoi han, chi so Story Points va danh sach cac Task thuc thi.
 */
@Getter
@Setter
@Builder
public class SprintDetailsResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY)
    // ======================================================

    /** ID dinh danh duy nhat cua Sprint trong CSDL. */
    private Integer id;

    /** ID cua Du an (Project) chua Sprint nay. */
    private Integer projectId;

    // ======================================================
    // 2. THONG TIN CO BAN (BASIC INFO)
    // ======================================================

    /** Ten Sprint (vi du: "Sprint 1 - Authentication"). */
    private String name;

    /** Muc tieu cot loi cua Sprint (Sprint Goal). */
    private String goal;

    /** Trang thai van hanh: NOT_STARTED, IN_PROGRESS, COMPLETED. */
    private SprintStatus status;

    // ======================================================
    // 3. THOI GIAN (TIMELINE)
    // ======================================================

    /** Thoi diem bat dau thuc te va thoi diem ket thuc du kien. */
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // ======================================================
    // 4. CHI SO VAN HANH (METRICS)
    // ======================================================

    /** * Tong Story Points cua toan bo Task trong Sprint. 
     * Dung de danh gia khoi luong cong viec (Workload). 
     */
    private Long totalStoryPoints;

    /** Tong so luong ban ghi Task co trong Sprint nay. */
    private Integer taskCount;

    // ======================================================
    // 5. NOI DUNG CONG VIEC (CONTENT)
    // ======================================================

    /** * Danh sach tom tat cac Task thuoc Sprint.
     * Dung de hien thi len man hinh Scrum Board hoac Sprint Backlog. 
     */
    private List<TaskSummaryResponse> tasks; 

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the Deserialize JSON mot cach minh bach.
     */
    public SprintDetailsResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public SprintDetailsResponse(Integer id, Integer projectId, String name, String goal, 
                                 SprintStatus status, LocalDateTime startDate, 
                                 LocalDateTime endDate, Long totalStoryPoints, 
                                 Integer taskCount, List<TaskSummaryResponse> tasks) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.goal = goal;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalStoryPoints = totalStoryPoints;
        this.taskCount = taskCount;
        this.tasks = tasks;
    }
}