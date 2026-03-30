package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin tom tat ve mot Khong gian lam viec (Workspace) ma nguoi dung tham gia.
 * Su dung chu yeu cho giao dien Dashboard de hien thi danh sach nhanh va phan quyen truy cap.
 */
@Getter
@Setter
@Builder
public class MyWorkspaceResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    // ======================================================
    // 1. THONG TIN KHONG GIAN LAM VIEC (WORKSPACE INFO)
    // ======================================================

    /** ID dinh danh duy nhat cua Workspace. */
    private Integer workspaceId;

    /** Ten hien thi (vi du: "Doi ngu Phat trien", "Marketing"). */
    private String workspaceName;

    /** Ma dinh danh viet tat (vi du: "DEV-TEAM"). */
    private String workspaceCode;

    /** Mo ta ngan gon ve chuc nang cua Workspace. */
    private String workspaceDescription;

    /** Duong dan URL den anh bia (Cover) de lam dep giao dien the (Card). */
    private String workspaceCoverImage;

    /** Ma mau dai dien (HEX) dung cho cac thanh UI hoac Avatar nhom. */
    private String workspaceColor;

    /** Trang thai van hanh: ACTIVE, ARCHIVED, v.v. */
    private String workspaceStatus;

    // ======================================================
    // 2. CONG TY CHU QUAN (PARENT COMPANY INFO)
    // ======================================================
    // Giup nguoi dung phan biet Workspace nay thuoc ve to chuc nao.

    /** ID va Ten cua Cong ty (Tenant) so huu Workspace nay. */
    private Integer companyId;
    private String companyName;

    /** Logo cua Cong ty de hien thi kem theo ten. */
    private String companyLogoUrl;

    // ======================================================
    // 3. TU CACH THANH VIEN (MEMBERSHIP INFO)
    // ======================================================

    /** * Ma vai tro (vi du: "WORKSPACE_ADMIN") va Ten hien thi (vi du: "Quan tri vien"). 
     * Frontend dung de an/hien cac nut chuc nang quan tri Workspace.
     */
    private String roleCode;
    private String roleName;

    /** Trang thai cua nguoi dung trong Workspace (ACTIVE, REMOVED). */
    private MemberStatus membershipStatus;

    /** Thoi diem nguoi dung chinh thuc gia nhap vao Workspace nay. */
    private LocalDateTime joinedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Giup Jackson Deserialize JSON mot cach minh bach.
     */
    public MyWorkspaceResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public MyWorkspaceResponse(Integer workspaceId, String workspaceName, String workspaceCode, 
                               String workspaceDescription, String workspaceCoverImage, 
                               String workspaceColor, String workspaceStatus, 
                               Integer companyId, String companyName, String companyLogoUrl, 
                               String roleCode, String roleName, 
                               MemberStatus membershipStatus, LocalDateTime joinedAt) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.workspaceCode = workspaceCode;
        this.workspaceDescription = workspaceDescription;
        this.workspaceCoverImage = workspaceCoverImage;
        this.workspaceColor = workspaceColor;
        this.workspaceStatus = workspaceStatus;
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyLogoUrl = companyLogoUrl;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.membershipStatus = membershipStatus;
        this.joinedAt = joinedAt;
    }
}