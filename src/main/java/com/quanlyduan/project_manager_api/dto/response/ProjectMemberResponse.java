package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet cua mot Thanh vien trong Du an (Project).
 * Duoc su dung de hien thi danh sach thanh vien, phan quyen hoac tim kiem nhan su trong noi bo du an.
 */
@Getter
@Setter
@Builder
public class ProjectMemberResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY)
    // ======================================================

    /** * ID ban ghi trong bang 'project_members'. 
     * Dung de thuc hien cac hanh dong quan tri nhu: Cap nhat vai tro, Xoa khoi du an. 
     */
    private Integer memberId;

    /** * ID tai khoan nguoi dung (User ID). 
     * Dung de lien ket den ho so ca nhan hoac truy van thong tin nguoi dung he thong. 
     */
    private Integer userId;

    // ======================================================
    // 2. THONG TIN CA NHAN (PERSONAL INFO)
    // ======================================================

    /** Ho va ten hien thi cua thanh vien. */
    private String fullName;

    /** Email lien he chinh thuc. */
    private String email;

    /** So dien thoai lien lac (neu co). */
    private String phoneNumber;

    /** Duong dan URL den anh dai dien (Avatar). */
    private String avatarUrl;

    // ======================================================
    // 3. NGU CANH TRONG DU AN (PROJECT CONTEXT)
    // ======================================================

    /** Ten vai tro hien tai (vi du: "Project Admin", "Developer", "Tester"). */
    private String roleName;

    /** Thoi diem thanh vien duoc moi hoac them vao du an nay. */
    private LocalDateTime joinedAt;

    /** * Trang thai hoat dong cua thanh vien trong du an.
     * Gia tri: ACTIVE (Dang tham gia), REMOVED (Da roi khoi), SUSPENDED (Tam dung).
     */
    private MemberStatus status;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the Deserialize du lieu mot cach minh bach.
     */
    public ProjectMemberResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public ProjectMemberResponse(Integer memberId, Integer userId, String fullName, 
                                 String email, String phoneNumber, String avatarUrl, 
                                 String roleName, LocalDateTime joinedAt, MemberStatus status) {
        this.memberId = memberId;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.roleName = roleName;
        this.joinedAt = joinedAt;
        this.status = status;
    }
}