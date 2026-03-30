package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO con (Nested DTO) chua thong tin ve tu cach thanh vien cua nguoi dung trong mot Du an.
 * Thuong duoc nhung vao danh sach "Du an cua toi" hoac thong tin Profile mo rong.
 */
@Getter
@Setter
@Builder
public class ProjectMembershipDTO {

    // ======================================================
    // 1. THONG TIN DU AN (PROJECT INFO)
    // ======================================================

    /** ID dinh danh duy nhat cua Du an. */
    private Integer projectId;

    /** Ten hien thi cua Du an (vi du: "App Mobile Phase 2"). */
    private String projectName;

    // ======================================================
    // 2. THONG TIN CAP CHA (HIERARCHY INFO)
    // ======================================================

    /** * ID cua Khong gian lam viec (Workspace) chua du an nay.
     * Frontend dung truong nay de tao duong dan Breadcrumb hoac link dieu huong quay lai Workspace.
     */
    private Integer workspaceId; 

    // ======================================================
    // 3. THONG TIN VAI TRO (ROLE INFO)
    // ======================================================

    /** * Ma vai tro cua nguoi dung trong du an nay.
     * Vi du: "PROJECT_ADMIN", "PROJECT_MEMBER", "GUEST".
     * Dung de phan quyen truy cap cac tinh nang ben trong du an.
     */
    private String roleCode;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the khoi tao doi tuong tu JSON mot cach minh bach.
     */
    public ProjectMembershipDTO() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public ProjectMembershipDTO(Integer projectId, String projectName, 
                                Integer workspaceId, String roleCode) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.workspaceId = workspaceId;
        this.roleCode = roleCode;
    }
}