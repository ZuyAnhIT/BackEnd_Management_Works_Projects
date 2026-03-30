package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet cua mot Loi moi tham gia Cong ty.
 * Ho tro Admin theo doi trang thai, thoi han va cung cap link moi truc tiep.
 */
@Getter
@Setter
@Builder
public class CompanyInvitationResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_ACCEPTED = "ACCEPTED";

    // ======================================================
    // 1. THONG TIN CO BAN (BASIC INFO)
    // ======================================================
    
    /** ID dinh danh duy nhat cua ban ghi loi moi. */
    private Integer id;

    /** Dia chi Email cua nguoi nhan loi moi. */
    private String email;

    /** Ten vai tro du kien (vi du: "COMPANY_MEMBER", "PROJECT_MANAGER"). */
    private String roleName;

    // ======================================================
    // 2. TRANG THAI & THOI HAN (STATUS & EXPIRY)
    // ======================================================

    /** * Trang thai hien tai cua loi moi. 
     * Gia tri: PENDING, EXPIRED, ACCEPTED.
     */
    private String status;

    /** Thoi diem loi moi se het han va khong con hieu luc. */
    private LocalDateTime expiresAt;

    /** Ten nguoi da thuc hien gui loi moi (thuong la Admin). */
    private String invitedByName;

    // ======================================================
    // 3. LIEN KET HANH DONG (ACTION LINKS)
    // ======================================================

    /** * Duong dan URL day du de chap nhan loi moi.
     * Dung de Admin copy va gui thu cong qua cac kenh chat (Zalo, Slack...).
     */
    private String invitationLink;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Giup Jackson Deserialize du lieu mot cach minh bach.
     */
    public CompanyInvitationResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay.
     * Bat buoc phai co de @Builder cua Lombok hoat dong on dinh.
     */
    public CompanyInvitationResponse(Integer id, String email, String roleName, 
                                     String status, LocalDateTime expiresAt, 
                                     String invitedByName, String invitationLink) {
        this.id = id;
        this.email = email;
        this.roleName = roleName;
        this.status = status;
        this.expiresAt = expiresAt;
        this.invitedByName = invitedByName;
        this.invitationLink = invitationLink;
    }
}