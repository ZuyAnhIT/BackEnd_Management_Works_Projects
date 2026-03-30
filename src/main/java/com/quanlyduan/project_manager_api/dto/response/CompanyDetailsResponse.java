package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet cua mot Cong ty (Tenant).
 * Duoc su dung de hien thi ho so doanh nghiep hoac phuc vu luong cap nhat thong tin.
 */
@Getter
@Setter
@Builder
public class CompanyDetailsResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY INFO)
    // ======================================================
    
    /** ID duy nhat cua cong ty trong he thong. */
    private Integer companyId;

    /** Ten hien thi chinh thuc cua doanh nghiep. */
    private String companyName;

    /** Ma dinh danh viet tat (vi du: "TECH", "WORKNET"). */
    private String companyCode;

    // ======================================================
    // 2. HO SO & THUONG HIEU (PROFILE & BRANDING)
    // ======================================================

    /** Mo ta ngan gon ve linh vuc hoac gioi thieu cong ty. */
    private String description;

    /** Duong dan URL den anh Logo da qua upload. */
    private String logo;

    // ======================================================
    // 3. LIEN HE & DIA DIEM (CONTACT & LOCATION)
    // ======================================================

    /** Dia chi tru so chinh hoac van phong dai dien. */
    private String address;

    /** So dien thoai lien lac chinh thuc. */
    private String phoneNumber;

    /** Dia chi email cham soc khach hang hoac email dai dien. */
    private String email;

    /** Dia chi trang web chinh thuc (Website). */
    private String website;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Giup Jackson Deserialize du lieu mot cach minh bach.
     */
    public CompanyDetailsResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay.
     * Bat buoc phai co de @Builder cua Lombok hoat dong on dinh.
     */
    public CompanyDetailsResponse(Integer companyId, String companyName, String companyCode, 
                                  String description, String logo, String address, 
                                  String phoneNumber, String email, String website) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyCode = companyCode;
        this.description = description;
        this.logo = logo;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.website = website;
    }
}