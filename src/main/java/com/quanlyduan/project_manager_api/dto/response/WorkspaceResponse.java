package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin tom tat cua mot Khong gian lam viec (Workspace).
 * Duoc su dung cho danh sach hien thi (List View) hoac thong tin tong quan khi vao Workspace.
 */
@Getter
@Setter
@Builder
public class WorkspaceResponse {

    // ======================================================
    // 1. DINH DANH & CAP BAC (IDENTITY & HIERARCHY)
    // ======================================================

    /** ID dinh danh duy nhat cua Workspace trong he thong. */
    private Integer workspaceId;

    /** ID cua Cong ty (Company) chu quan chua Workspace nay. */
    private Integer companyId;

    // ======================================================
    // 2. THONG TIN HIEN THI (BASIC INFO & VISUALS)
    // ======================================================

    /** Ten hien thi cua Workspace (vi du: "Phong Ky thuat", "Doi Marketing"). */
    private String workspaceName;

    /** Mo ta ngan gon ve muc dich hoac nhiem vu cua Workspace. */
    private String description;
    
    /** Duong dan URL den anh bia (Cover Image) de lam dep giao dien. */
    private String coverImage;

    /** Ma mau HEX dai dien (vi du: "#3498db") dung de phan biet nhanh tren Sidebar. */
    private String color;

    // ======================================================
    // 3. TRANG THAI & HE THONG (STATE & AUDIT)
    // ======================================================

    /** ID cua nguoi dung da khoi tao Workspace nay. */
    private Integer createdById;

    /** Trang thai hoat dong (vi du: ACTIVE, ARCHIVED, DELETED). */
    private String status;

    /** Thoi diem ban ghi duoc tao lap. */
    private LocalDateTime createdAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson Deserialize JSON mot cach minh bach.
     */
    public WorkspaceResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public WorkspaceResponse(Integer workspaceId, Integer companyId, String workspaceName, 
                             String description, String coverImage, String color, 
                             Integer createdById, String status, LocalDateTime createdAt) {
        this.workspaceId = workspaceId;
        this.companyId = companyId;
        this.workspaceName = workspaceName;
        this.description = description;
        this.coverImage = coverImage;
        this.color = color;
        this.createdById = createdById;
        this.status = status;
        this.createdAt = createdAt;
    }
}