package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin cau hinh cua mot Trang thai (Cot) trong Du an.
 * Duoc su dung de ve giao dien Board (Kanban/Scrum) va thiet lap quy trinh lam viec (Workflow).
 */
@Getter
@Setter
@Builder
public class ProjectStatusResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY)
    // ======================================================

    /** ID dinh danh duy nhat cua Trang thai trong CSDL. */
    private Integer id;

    /** ID cua Du an, Workspace va Cong ty chu quan. */
    private Integer projectId;
    private Integer workspaceId; 
    private Integer companyId;

    // ======================================================
    // 2. THONG TIN HIEN THI (DISPLAY INFO)
    // ======================================================

    /** Ten hien thi cua cot trang thai (vi du: "To Do", "In Progress", "Done"). */
    private String name;

    /** Ma mau HEX dung de to mau cho tieu de cot hoac nhan (vi du: "#3498db"). */
    private String color;

    // ======================================================
    // 3. THONG TIN CAU HINH (CONFIG INFO)
    // ======================================================

    /** * Thu tu sap xep cua cot tren giao dien Board (0, 1, 2...). 
     * Cot co gia tri nho hon se duoc hien thi ben trai.
     */
    private Integer sortOrder;

    /** * Co danh dau day la trang thai cuoi cung (Hoan thanh).
     * true: Task thuoc cot nay duoc tinh la hoan thanh (Progress 100%).
     */
    private Boolean isCompletedStatus;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the Deserialize JSON mot cach minh bach.
     */
    public ProjectStatusResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public ProjectStatusResponse(Integer id, Integer projectId, Integer workspaceId, 
                                 Integer companyId, String name, String color, 
                                 Integer sortOrder, Boolean isCompletedStatus) {
        this.id = id;
        this.projectId = projectId;
        this.workspaceId = workspaceId;
        this.companyId = companyId;
        this.name = name;
        this.color = color;
        this.sortOrder = sortOrder;
        this.isCompletedStatus = isCompletedStatus;
    }
}