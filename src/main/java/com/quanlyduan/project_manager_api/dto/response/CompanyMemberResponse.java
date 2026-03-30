package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;

import com.quanlyduan.project_manager_api.model.common.enums.CombinedMemberStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet cua mot Thanh vien trong Cong ty.
 * Duoc su dung chung cho ca Thanh vien chinh thuc va Loi moi dang cho (Pending).
 */
@Getter
@Setter
@Builder
public class CompanyMemberResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY)
    // ======================================================

    /** * ID ban ghi trong bang 'company_members'.
     * Luu y: Se la NULL neu day la mot loi moi (Pending Invitation) chua duoc chap nhan. 
     */
    private Integer memberId; 

    /** * ID tai khoan nguoi dung (User ID).
     * Luu y: Se la NULL neu nguoi duoc moi chua co tai khoan tren he thong. 
     */
    private Integer userId;

    // ======================================================
    // 2. THONG TIN CA NHAN (PERSONAL INFO)
    // ======================================================

    /** Ho va ten hien thi tren giao dien. */
    private String fullName; 

    /** Email lien he (Dung de tra cuu hoac gui thong bao). */
    private String email;

    /** So dien thoai lien lac. */
    private String phoneNumber;

    /** Duong dan URL den anh dai dien (Avatar). */
    private String avatarUrl; 
    
    // ======================================================
    // 3. VAI TRO & THOI GIAN (ROLE & TIMELINE)
    // ======================================================

    /** Ten vai tro trong cong ty (vi du: Admin, Member). */
    private String roleName; 
    
    /** Chuc danh cong viec cu the (vi du: Backend Developer). */
    private String jobTitle; 

    /** Thoi diem tham gia (official) hoac thoi diem gui loi moi (pending). */
    private LocalDateTime joinedAt; 
    
    // ======================================================
    // 4. TRANG THAI (STATUS)
    // ======================================================

    /** * Trang thai tong hop cua thanh vien.
     * Gia tri: ACTIVE, SUSPENDED, REMOVED, PENDING.
     */
    private CombinedMemberStatus status;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public CompanyMemberResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public CompanyMemberResponse(Integer memberId, Integer userId, String fullName, 
                                 String email, String phoneNumber, String avatarUrl, 
                                 String roleName, String jobTitle, LocalDateTime joinedAt, 
                                 CombinedMemberStatus status) {
        this.memberId = memberId;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.roleName = roleName;
        this.jobTitle = jobTitle;
        this.joinedAt = joinedAt;
        this.status = status;
    }
}