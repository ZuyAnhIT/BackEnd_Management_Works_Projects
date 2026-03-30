package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin tom tat ve mot Cong ty ma nguoi dung dang tham gia.
 * Thuong duoc su dung trong trang Dashboard (Tong quan) hoac danh sach "Cong ty cua toi".
 */
@Getter
@Setter
@Builder
public class MyCompanyResponse {

    // ======================================================
    // 1. THONG TIN CONG TY (COMPANY INFO)
    // ======================================================

    /** ID dinh danh duy nhat cua Cong ty. */
    private Integer companyId;

    /** Ten hien thi chinh thuc (vi du: "Worknet JSC"). */
    private String companyName;

    /** Ma code viet tat cua Cong ty (vi du: "WNK-V"). */
    private String companyCode;

    /** Mo ta ngan gon ve linh vuc hoac ton chi cua to chuc. */
    private String description;

    /** Duong dan URL den anh Logo de hien thi tren danh sach. */
    private String logoUrl;

    // ======================================================
    // 2. TU CACH THANH VIEN (MEMBERSHIP INFO)
    // ======================================================

    /** * Ma vai tro cua nguoi dung (vi du: "COMPANY_ADMIN", "MEMBER"). 
     * Frontend dung de hien thi Badge phan quyen.
     */
    private String roleCode;

    /** * Trang thai thanh vien (vi du: "ACTIVE", "SUSPENDED"). 
     * Dung de to mau trang thai (Xanh/Do) tren giao dien.
     */
    private String memberStatus;

    /** Chuc danh nghe nghiep (vi du: "Senior Developer"). */
    private String jobTitle;

    /** Phong ban dang cong tac (dang van ban). */
    private String department;

    /** Ngay nguoi dung chinh thuc gia nhap to chuc. */
    private LocalDateTime joinedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson va cac thu vien Mapping hoat dong on dinh.
     */
    public MyCompanyResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong minh bach.
     */
    public MyCompanyResponse(Integer companyId, String companyName, String companyCode, 
                             String description, String logoUrl, String roleCode, 
                             String memberStatus, String jobTitle, String department, 
                             LocalDateTime joinedAt) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyCode = companyCode;
        this.description = description;
        this.logoUrl = logoUrl;
        this.roleCode = roleCode;
        this.memberStatus = memberStatus;
        this.jobTitle = jobTitle;
        this.department = department;
        this.joinedAt = joinedAt;
    }
}