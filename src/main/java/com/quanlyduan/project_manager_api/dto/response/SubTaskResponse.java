package com.quanlyduan.project_manager_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet cua mot SubTask (Cong viec phu).
 * SubTask luon gan lien voi mot Task cha, dung de chia nho khoi luong cong viec lon.
 */
@Getter
@Setter
@Builder
public class SubTaskResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY)
    // ======================================================

    /** ID dinh danh duy nhat cua SubTask. */
    private Integer id;

    /** ID cua Du an (Project) chua SubTask nay. */
    private Integer projectId;

    /** * ID cua Task cha (Parent Task). 
     * Day la truong quan trong de lien ket SubTask vao dung ngu canh cong viec chinh. 
     */
    private Integer parentTaskId;

    // ======================================================
    // 2. THONG TIN CO BAN (BASIC INFO)
    // ======================================================

    /** Tieu de ngan gon cua cong viec phu. */
    private String title;

    /** Mo ta chi tiet cac buoc can thuc hien. */
    private String description;

    /** * Thu tu sap xep ben trong danh sach SubTask cua Task cha. 
     * Dung de keo tha (Drag & Drop) sap xep thu tu thuc hien.
     */
    private Integer sortOrder;

    // ======================================================
    // 3. TRANG THAI & TIEN DO (STATUS & PROGRESS)
    // ======================================================

    /** Trang thai hien tai (vi du: TO_DO, IN_PROGRESS, DONE). */
    private SubTaskStatus status;

    /** Thoi gian uoc tinh de hoan thanh (don vi: Gio). */
    private BigDecimal estimatedHours;

    // ======================================================
    // 4. THONG TIN NHAN SU (ASSIGNMENT)
    // ======================================================

    /** Thong tin nguoi thuc hien cong viec phu nay. */
    private Integer assigneeId;
    private String assigneeName;
    private String assigneeAvatar;

    // ======================================================
    // 5. THONG TIN HE THONG (AUDIT)
    // ======================================================

    /** Thong tin nguoi khoi tao ban ghi. */
    private Integer createdById;
    private String createdByName;

    /** Thoi diem tao va lan cap nhat cuoi cung. */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the Deserialize du lieu mot cach minh bach.
     */
    public SubTaskResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public SubTaskResponse(Integer id, Integer projectId, Integer parentTaskId, String title, 
                           String description, Integer sortOrder, SubTaskStatus status, 
                           BigDecimal estimatedHours, Integer assigneeId, String assigneeName, 
                           String assigneeAvatar, Integer createdById, String createdByName, 
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.parentTaskId = parentTaskId;
        this.title = title;
        this.description = description;
        this.sortOrder = sortOrder;
        this.status = status;
        this.estimatedHours = estimatedHours;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.assigneeAvatar = assigneeAvatar;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}