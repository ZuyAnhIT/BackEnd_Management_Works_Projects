package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin quan ly Loi moi tham gia Du an.
 * Dung trong danh sach quan tri de Admin theo doi, huy hoac gui lai (resend) loi moi.
 */
@Getter
@Setter
@Builder
public class ProjectInvitationResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_ACCEPTED = "ACCEPTED";

    // ======================================================
    // 1. THONG TIN LOI MOI (INVITATION META)
    // ======================================================
    
    /** ID dinh danh cua ban ghi loi moi (dung de Huy hoac Gui lai). */
    private Integer id;

    /** Email cua nguoi nhan loi moi. */
    private String email;

    /** Ma vai tro du kien se gan (vi du: MEMBER, GUEST, DEVELOPER). */
    private String roleCode;

    /** * Trang thai hien tai cua loi moi.
     * Gia tri: PENDING, EXPIRED, ACCEPTED. 
     */
    private String status;

    /** Thoi diem gui loi moi lan dau tien. */
    private LocalDateTime invitedAt;

    /** * Duong dan URL moi truc tiep.
     * Ho tro Admin copy va gui thu cong qua Zalo/Slack khi Email bi that lac.
     */
    private String invitationLink;

    // ======================================================
    // 2. NGUOI GUI LOI MOI (INVITER INFO - OPTIONAL)
    // ======================================================
    
    /** Ho ten va Anh dai dien cua nguoi thuc hien moi (thuong la Project Admin). */
    private String inviterName;
    private String inviterAvatar;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public ProjectInvitationResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public ProjectInvitationResponse(Integer id, String email, String roleCode, String status, 
                                    LocalDateTime invitedAt, String invitationLink, 
                                    String inviterName, String inviterAvatar) {
        this.id = id;
        this.email = email;
        this.roleCode = roleCode;
        this.status = status;
        this.invitedAt = invitedAt;
        this.invitationLink = invitationLink;
        this.inviterName = inviterName;
        this.inviterAvatar = inviterAvatar;
    }
}