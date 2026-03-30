package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin co ban cua mot Sprint.
 * Duoc su dung trong cac thao tac CRUD (Tao, Cap nhat, Bat dau Sprint) de phan hoi trang thai moi nhat.
 */
@Getter
@Setter
@Builder
public class SprintResponse {

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

    /** Ten goi cua Sprint (vi du: "Sprint 1", "Sprint Alpha"). */
    private String name;

    /** Muc tieu ngan han can dat duoc trong Sprint (Sprint Goal). */
    private String goal;

    /** Trang thai hien tai (vi du: NOT_STARTED, IN_PROGRESS, COMPLETED). */
    private String status;

    // ======================================================
    // 3. THOI GIAN (TIMELINE)
    // ======================================================

    /** Thoi diem bat dau du kien/thuc te va thoi diem ket thuc. */
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // ======================================================
    // 4. DANH SACH CONG VIEC (TASK LIST)
    // ======================================================

    /** * Danh sach cac Task thuoc Sprint nay.
     * Dung de cap nhat lai State cua danh sach cong viec tai Frontend sau khi CRUD Sprint.
     */
    private List<TaskResponse> tasks;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Giup Jackson Deserialize JSON mot cach minh bach.
     */
    public SprintResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public SprintResponse(Integer id, Integer projectId, String name, String goal, 
                          String status, LocalDateTime startDate, LocalDateTime endDate, 
                          List<TaskResponse> tasks) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.goal = goal;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tasks = tasks;
    }
}