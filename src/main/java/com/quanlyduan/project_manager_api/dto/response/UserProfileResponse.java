package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.quanlyduan.project_manager_api.model.common.enums.Gender;
import com.quanlyduan.project_manager_api.model.common.enums.UserStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi Ho so nguoi dung day du (Full User Profile).
 * Tong hop tat ca thong tin ca nhan va tu cach thanh vien (Memberships) 
 * tu cac cap bac: Company, Workspace, Project.
 */
@Getter
@Setter
@Builder
public class UserProfileResponse {

    // ======================================================
    // 1. DINH DANH & HE THONG (IDENTITY & AUDIT)
    // ======================================================
    
    /** ID dinh danh duy nhat cua nguoi dung. */
    private Integer id;

    /** Dia chi Email dang nhap. */
    private String email;
    
    /** Trang thai tai khoan (vi du: ACTIVE, LOCKED). */
    private UserStatus status;

    /** Co xac thuc Email (giup Frontend hien thi badge "Da xac minh"). */
    private boolean isEmailVerified;

    /** Thoi diem tao tai khoan va lan truy cap cuoi cung. */
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    // ======================================================
    // 2. THONG TIN CA NHAN (PERSONAL DETAILS)
    // ======================================================

    /** Ho ten, Anh dai dien va thong tin lien lac. */
    private String fullName;
    private String avatarUrl;
    private String phoneNumber;

    /** Ngay sinh va Gioi tinh (dung de ca nhan hoa trai nghiem). */
    private LocalDate dateOfBirth;
    private Gender gender;

    // ======================================================
    // 3. VAI TRO & TU CACH THANH VIEN (ROLES & MEMBERSHIPS)
    // ======================================================

    /** Danh sach vai tro cap he thong (vi du: ["SYSTEM_ADMIN", "USER"]). */
    private List<String> systemRoles;

    /** * Danh sach tu cach thanh vien tai cac Cong ty. 
     * Bao gom thong tin Cong ty va Vai tro trong do. 
     */
    private List<CompanyMembershipDTO> companyMemberships;

    /** Danh sach cac Workspace ma nguoi dung co quyen truy cap. */
    private List<WorkspaceMembershipDTO> workspaceMemberships;

    /** Danh sach cac Du an (Project) ma nguoi dung dang tham gia. */
    private List<ProjectMembershipDTO> projectMemberships;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson Deserialize JSON mot cach minh bach.
     */
    public UserProfileResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public UserProfileResponse(Integer id, String email, UserStatus status, boolean isEmailVerified, 
                                LocalDateTime createdAt, LocalDateTime lastLoginAt, String fullName, 
                                String avatarUrl, String phoneNumber, LocalDate dateOfBirth, 
                                Gender gender, List<String> systemRoles, 
                                List<CompanyMembershipDTO> companyMemberships, 
                                List<WorkspaceMembershipDTO> workspaceMemberships, 
                                List<ProjectMembershipDTO> projectMemberships) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.isEmailVerified = isEmailVerified;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.systemRoles = systemRoles;
        this.companyMemberships = companyMemberships;
        this.workspaceMemberships = workspaceMemberships;
        this.projectMemberships = projectMemberships;
    }
}