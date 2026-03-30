package com.quanlyduan.project_manager_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet cua mot Du an (Project).
 * Giup bao mat Entity, tranh loi de quy va cung cap du lieu tinh gon cho giao dien.
 */
@Getter
@Setter
@Builder
public class ProjectResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY & HIERARCHY)
    // ======================================================

    /** ID dinh danh duy nhat cua Du an. */
    private Integer id;

    /** ID cua Khong gian lam viec (Workspace) va Cong ty (Company) chu quan. */
    private Integer workspaceId;
    private Integer companyId;

    // ======================================================
    // 2. THONG TIN CO BAN (BASIC INFO)
    // ======================================================

    /** Ten hien thi va Ma code dinh danh (vi du: "Web App", "PRJ-001"). */
    private String name;
    private String projectCode;

    /** Mo ta chi tiet va Muc tieu cot loi cua du an. */
    private String description;
    private String goal;

    /** Duong dan URL den anh bia (Cover Image) cua du an. */
    private String coverImageUrl;

    // ======================================================
    // 3. TRANG THAI & TIEN ĐO (STATUS & PROGRESS)
    // ======================================================

    /** Trang thai hien tai (vi du: OPEN, IN_PROGRESS, DONE). */
    private String status;

    /** Muc do uu tien (vi du: LOW, MEDIUM, HIGH, URGENT). */
    private String priority;

    /** * Tien do hoan thanh tong the (Scale 0-100%). 
     * Duoc tinh toan dua tren khoi luong cong viec hoac Story Points. 
     */
    private BigDecimal progress;

    // ======================================================
    // 4. THOI GIAN (TIMELINE)
    // ======================================================

    /** Ngay bat dau du kien va Han chot (Deadline). */
    private LocalDate startDate;
    private LocalDate dueDate;

    /** Ngay thuc te du an duoc chuyen sang trang thai hoan thanh. */
    private LocalDate completedAt;

    // ======================================================
    // 5. THONG TIN NHAN SU (PEOPLE)
    // ======================================================

    /** Thong tin Nguoi quan ly du an (Project Manager). */
    private Integer managerId;
    private String managerName;

    /** Thong tin Nguoi khoi tao du an trong he thong. */
    private Integer createdById;
    private String createdByName;

    // ======================================================
    // 6. THONG TIN HE THONG (AUDIT)
    // ======================================================

    /** Thoi diem ban ghi duoc tao va lan cap nhat cuoi cung. */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the Deserialize du lieu mot cach minh bach.
     */
    public ProjectResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public ProjectResponse(Integer id, Integer workspaceId, Integer companyId, String name, 
                           String projectCode, String description, String goal, 
                           String coverImageUrl, String status, String priority, 
                           BigDecimal progress, LocalDate startDate, LocalDate dueDate, 
                           LocalDate completedAt, Integer managerId, String managerName, 
                           Integer createdById, String createdByName, 
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.companyId = companyId;
        this.name = name;
        this.projectCode = projectCode;
        this.description = description;
        this.goal = goal;
        this.coverImageUrl = coverImageUrl;
        this.status = status;
        this.priority = priority;
        this.progress = progress;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.completedAt = completedAt;
        this.managerId = managerId;
        this.managerName = managerName;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}