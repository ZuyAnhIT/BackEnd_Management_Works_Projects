package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO con (Nested DTO) chua thong tin ve tu cach thanh vien cua nguoi dung trong mot Cong ty.
 * Thuong duoc nhung vao danh sach "Cong ty cua toi" hoac thong tin Profile mo rong.
 */
@Getter
@Setter
@Builder
public class CompanyMembershipDTO {

    // ======================================================
    // 1. THONG TIN CONG TY (COMPANY IDENTITY)
    // ======================================================

    /** ID dinh danh duy nhat cua Cong ty. */
    private Integer companyId;

    /** Ten hien thi cua Cong ty (vi du: "Cong nghe Toan Cau"). */
    private String companyName; 

    // ======================================================
    // 2. TRANG THAI THANH VIEN (MEMBERSHIP STATUS)
    // ======================================================

    /** * Ma vai tro cua nguoi dung hien tai trong Cong ty nay.
     * Vi du: "COMPANY_ADMIN", "COMPANY_MEMBER".
     * Frontend dung truong nay de an/hien cac nut chuc nang quan tri (Settings, Billing).
     */
    private String roleCode; 

    /** Trang thai hoat dong cua Cong ty (vi du: "ACTIVE", "SUSPENDED"). */
    private String companyStatus;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the khoi tao doi tuong tu JSON mot cach an toan.
     */
    public CompanyMembershipDTO() {
    }

    /**
     * Constructor day du (All-args) viet tay.
     * Bat buoc phai co de @Builder cua Lombok hoat dong dung cach.
     */
    public CompanyMembershipDTO(Integer companyId, String companyName, 
                                String roleCode, String companyStatus) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.roleCode = roleCode;
        this.companyStatus = companyStatus;
    }
}