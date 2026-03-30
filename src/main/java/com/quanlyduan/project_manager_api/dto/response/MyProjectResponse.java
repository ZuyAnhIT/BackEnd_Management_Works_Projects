package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO chua thong tin tom tat ve Du an (Project) ma nguoi dung dang tham gia.
 * Duoc su dung chu yeu trong API Dashboard de hien thi danh sach nhanh tren the (Card).
 */
@Getter
@Setter
@Builder
public class MyProjectResponse {

    // ======================================================
    // 1. THONG TIN DU AN (PROJECT INFO)
    // ======================================================

    /** ID dinh danh duy nhat cua Du an. */
    private Integer projectId;

    /** Ten hien thi cua Du an (vi du: "He thong CRM Phase 1"). */
    private String projectName;

    /** Mo ta ngan gon ve muc tieu cua du an. */
    private String description;

    /** Duong dan URL den anh bia (Cover Image) cua du an. */
    private String coverImage;

    /** Ma mau dai dien (HEX) dung de hien thi Tag hoac mau chu de cho the Project. */
    private String color;

    // ======================================================
    // 2. THONG TIN CAP CHA (HIERARCHY INFO)
    // ======================================================
    // Giup nguoi dung biet du an nay thuoc ve "Nha" nao

    /** ID va Ten cua Khong gian lam viec (Workspace/Phong ban) truc thuoc. */
    private Integer workspaceId;
    private String workspaceName;

    /** ID va Ten cua Cong ty (Tenant) so huu du an nay. */
    private Integer companyId;
    private String companyName;

    // ======================================================
    // 3. NGU CANH NGUOI DUNG (USER CONTEXT)
    // ======================================================

    /** * Ten vai tro cua nguoi dung hien tai trong dự án này.
     * Vi du: "PROJECT_ADMIN", "MEMBER", "STAKEHOLDER".
     * Frontend dung de phan quyen hien thi cac nut chuc nang tren Card.
     */
    private String myRoleName;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the Deserialize du lieu mot cach minh bach.
     */
    public MyProjectResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public MyProjectResponse(Integer projectId, String projectName, String description, 
                             String coverImage, String color, Integer workspaceId, 
                             String workspaceName, Integer companyId, String companyName, 
                             String myRoleName) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.description = description;
        this.coverImage = coverImage;
        this.color = color;
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.companyId = companyId;
        this.companyName = companyName;
        this.myRoleName = myRoleName;
    }
}