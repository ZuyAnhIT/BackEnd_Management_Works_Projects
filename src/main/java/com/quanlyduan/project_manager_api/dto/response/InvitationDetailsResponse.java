package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi chi tiet cua mot loi moi khi nguoi dung nhap vao link tu Email.
 * Day la API cong khai (Public) giup Frontend dieu huong nguoi dung den trang Dang nhap hoac Dang ky.
 */
@Getter
@Setter
@Builder
public class InvitationDetailsResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY INFO)
    // ======================================================

    /** * Email cua nguoi duoc moi. 
     * Frontend dung truong nay de tu dong dien vao o Email va khoa (readonly) de dam bao tinh bao mat.
     */
    private String email;

    /** * Ten cong ty da gui loi moi. 
     * Dung de hien thi loi chao: "Chao mung ban den voi [companyName] tren Worknet".
     */
    private String companyName;

    // ======================================================
    // 2. LOGIC DIEU HUONG (NAVIGATION LOGIC)
    // ======================================================

    /** * Co kiem tra su ton tai cua tai khoan trong he thong.
     * - true: Da co tai khoan -> Frontend hien thi nut "Dang nhap de tiep tuc".
     * - false: Chua co tai khoan -> Frontend hien thi form "Hoan tat dang ky".
     */
    private boolean accountExists;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the khoi tao doi tuong tu JSON mot cach minh bach.
     */
    public InvitationDetailsResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public InvitationDetailsResponse(String email, String companyName, boolean accountExists) {
        this.email = email;
        this.companyName = companyName;
        this.accountExists = accountExists;
    }
}