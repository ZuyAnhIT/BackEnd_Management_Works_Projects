package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet cua mot Thanh vien trong Khong gian lam viec (Workspace).
 * Duoc su dung de hien thi danh sach nhan su, quan ly phan quyen va trang thai hoat dong trong Workspace.
 */
@Getter
@Setter
@Builder
public class WorkspaceMemberResponse {

    // ======================================================
    // 1. DINH DANH & CA NHAN (IDENTITY & PERSONAL INFO)
    // ======================================================

    /** * ID ban ghi thanh vien trong Workspace. 
     * Dung cho cac thao tac quan tri nhu: Cap nhat vai tro, Xoa khoi Workspace. 
     */
    private Integer memberId;

    /** * ID tai khoan nguoi dung (User ID). 
     * Dung de lien ket den ho so ca nhan hoac thuc hien cac tac vu xuyen suot he thong. 
     */
    private Integer userId;
    
    /** Ho va ten day du cua thanh vien. */
    private String fullName;

    /** Dia chi Email lien he chinh thuc. */
    private String email;

    /** So dien thoai lien lac (neu co). */
    private String phoneNumber; 
    
    /** Duong dan URL den anh dai dien (Avatar). */
    private String avatarUrl;

    // ======================================================
    // 2. NGU CANH WORKSPACE (WORKSPACE CONTEXT)
    // ======================================================

    /** Ten vai tro trong Workspace (vi du: "Workspace Admin", "Member"). */
    private String roleName;

    /** * Trang thai hoat dong cua thanh vien ben trong Workspace nay. 
     * Gia tri: ACTIVE (Dang hoat dong), REMOVED (Da roi khoi). 
     */
    private MemberStatus status;

    /** Thoi diem thanh vien bat dau tham gia vao Workspace. */
    private LocalDateTime joinedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson Deserialize JSON mot cach minh bach.
     */
    public WorkspaceMemberResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public WorkspaceMemberResponse(Integer memberId, Integer userId, String fullName, 
                                   String email, String phoneNumber, String avatarUrl, 
                                   String roleName, MemberStatus status, LocalDateTime joinedAt) {
        this.memberId = memberId;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.roleName = roleName;
        this.status = status;
        this.joinedAt = joinedAt;
    }
}