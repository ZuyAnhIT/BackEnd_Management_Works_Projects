package com.quanlyduan.project_manager_api.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi du lieu tong hop cho man hinh Backlog (Scrum/Agile).
 * Cau truc gom: Danh sach cac Sprint dang thuc thi va Danh sach Cong viec ton dong (Product Backlog).
 */
@Getter
@Setter
@Builder
public class ProjectBacklogResponse {

    // ======================================================
    // 1. CAC SPRINT DANG HOAT DONG (ACTIVE SPRINTS)
    // ======================================================
    
    /** * Danh sach cac Sprint dang trong trang thai IN_PROGRESS hoac NOT_STARTED. 
     * Moi Sprint da bao gom danh sach cac Task con ben trong. 
     */
    private List<SprintDetailsResponse> activeSprints;
    
    // ======================================================
    // 2. DANH SACH CONG VIEC TON ĐONG (PRODUCT BACKLOG)
    // ======================================================

    /** * Danh sach cac Task chua duoc gan vao bat ky Sprint nao (Product Backlog).
     * Phan nay duoc phan trang de dam bao hieu nang khi so luong Task lon.
     */
    private List<TaskSummaryResponse> backlogTasks;

    // ======================================================
    // 3. METADATA PHAN TRANG (BACKLOG PAGINATION)
    // ======================================================

    /** Chi so trang hien tai cua danh sach Backlog (0-based). */
    private int backlogPageNumber;

    /** Kich thuoc trang (So luong Task tra ve trong mot lan load). */
    private int backlogPageSize;

    /** Tong so luong Task hien co trong Product Backlog. */
    private long backlogTotalElements;

    /** Tong so trang co the tai (totalElements / pageSize). */
    private int backlogTotalPages;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Giup Jackson Deserialize du lieu mot cach minh bach.
     */
    public ProjectBacklogResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public ProjectBacklogResponse(List<SprintDetailsResponse> activeSprints, 
                                  List<TaskSummaryResponse> backlogTasks, 
                                  int backlogPageNumber, int backlogPageSize, 
                                  long backlogTotalElements, int backlogTotalPages) {
        this.activeSprints = activeSprints;
        this.backlogTasks = backlogTasks;
        this.backlogPageNumber = backlogPageNumber;
        this.backlogPageSize = backlogPageSize;
        this.backlogTotalElements = backlogTotalElements;
        this.backlogTotalPages = backlogTotalPages;
    }
}