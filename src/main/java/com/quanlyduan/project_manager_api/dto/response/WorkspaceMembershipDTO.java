package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO con (Nested DTO) chua thong tin ve tu cach thanh vien cua nguoi dung trong mot Workspace.
 * Thuong duoc nhung vao danh sach "Workspace cua toi" hoac Ho so nguoi dung (UserProfileResponse).
 */
@Getter
@Setter
@Builder
public class WorkspaceMembershipDTO {

    // ======================================================
    // 1. THONG TIN WORKSPACE (WORKSPACE INFO)
    // ======================================================

    /** ID dinh danh duy nhat cua Khong gian lam viec (Workspace). */
    private Integer workspaceId;

    /** Ten hien thi cua Workspace (vi du: "Phong Cong nghe", "Ban Marketing"). */
    private String workspaceName;

    // ======================================================
    // 2. THONG TIN CAP CHA (HIERARCHY INFO)
    // ======================================================

    /** * ID cua Cong ty (Company) chu quan chua Workspace nay.
     * Dung de xac dinh ngu canh cha khi thuc hien dieu huong hoac kiem tra han muc.
     */
    private Integer companyId; 

    // ======================================================
    // 3. THONG TIN VAI TRO (ROLE INFO)
    // ======================================================

    /** * Ma vai tro cua nguoi dung trong Workspace nay.
     * Gia tri: "WORKSPACE_ADMIN", "WORKSPACE_MEMBER".
     * Dung de phan quyen cac thao tac ben trong pham vi Workspace.
     */
    private String roleCode;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the Deserialize du lieu mot cach minh bach.
     */
    public WorkspaceMembershipDTO() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public WorkspaceMembershipDTO(Integer workspaceId, String workspaceName, 
                                  Integer companyId, String roleCode) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.companyId = companyId;
        this.roleCode = roleCode;
    }
}