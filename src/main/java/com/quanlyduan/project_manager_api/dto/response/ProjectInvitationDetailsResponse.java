package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi chi tiet cua mot Loi moi tham gia Du an.
 * Duoc su dung khi nguoi dung click vao link tu Email de hien thi thong tin chao mung
 * va dieu huong luong tiep theo (Dang ky hoac Dang nhap).
 */
@Getter
@Setter
@Builder
public class ProjectInvitationDetailsResponse {

    // ======================================================
    // 1. THONG TIN NGU CANH (INVITATION CONTEXT)
    // ======================================================

    /** * Email cua nguoi duoc moi. 
     * Frontend dung de pre-fill vao form va khoa (readonly) de bao mat. 
     */
    private String email;

    /** * Ten Du an (Project) ma ho duoc moi tham gia. 
     * Dung de hien thi: "Ban duoc moi vao du an [projectName]".
     */
    private String projectName;

    /** * Ten vai tro du kien se duoc gan (vi du: "Developer", "Tester").
     * Giup nguoi dung biet quyen han cua minh truoc khi chap nhan.
     */
    private String roleName;

    // ======================================================
    // 2. LOGIC DIEU HUONG & THOI HAN (LOGIC & TIMELINE)
    // ======================================================

    /** * Co kiem tra tai khoan da ton tai trong he thong hay chua.
     * - true: Da co tai khoan -> Frontend hien thi trang Dang nhap.
     * - false: Chua co tai khoan -> Frontend hien thi trang Dang ky moi.
     */
    private boolean accountExists;

    /** * Thoi gian het han cua ma moi (Token).
     * Frontend dung de canh bao neu loi moi da qua han hoac hien thi dem nguoc.
     */
    private LocalDateTime expiresAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Giup Jackson Deserialize du lieu mot cach minh bach.
     */
    public ProjectInvitationDetailsResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public ProjectInvitationDetailsResponse(String email, String projectName, String roleName, 
                                            boolean accountExists, LocalDateTime expiresAt) {
        this.email = email;
        this.projectName = projectName;
        this.roleName = roleName;
        this.accountExists = accountExists;
        this.expiresAt = expiresAt;
    }
}