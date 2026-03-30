package com.quanlyduan.project_manager_api.service;

/**
 * Service chuyen trach kiem tra han muc tai nguyen (Quota Validation) cua Cong ty.
 * Thuc hien doi soat giua su dung thuc te va gioi han cua Goi cuoc (Subscription Plan).
 * Neu vuot qua han muc, he thong se nem ra OverageException (HTTP 402 Payment Required).
 */
public interface QuotaValidationService {

    // ======================================================
    // 1. HAN MUC CAU TRUC (STRUCTURAL QUOTA)
    // ======================================================

    /**
     * Kiem tra xem Cong ty co duoc phep khoi tao them Du an moi hay khong.
     * Logic dua tren so luong Project hien tai so voi 'max_projects' cua Goi cuoc.
     * * @param companyId ID dinh danh cua Cong ty can kiem tra
     * @throws OverageException Neu so luong du an da dat den gioi han toi da
     */
    void validateProjectCreationQuota(Integer companyId);

    /**
     * Kiem tra xem Cong ty co duoc phep tao them Khong gian lam viec (Workspace) moi hay khong.
     * Logic dua tren so luong Workspace hien tai so voi 'max_workspaces' cua Goi cuoc.
     * * @param companyId ID dinh danh cua Cong ty can kiem tra
     * @throws OverageException Neu so luong khong gian lam viec da dat den gioi han
     */
    void validateWorkspaceCreationQuota(Integer companyId);
    
    // ======================================================
    // 2. HAN MUC NHAN SU VA LUU TRU (RESOURCE QUOTA)
    // ======================================================

    /**
     * Kiem tra gioi han so luong Thanh vien (User) toi da cua doanh nghiep.
     * Bat buoc thuc hien truoc khi gui email moi (Invite) thanh vien moi gia nhap.
     * * @param companyId ID dinh danh cua Cong ty
     * @throws OverageException Neu tong so thanh vien (da active + dang cho) vuot qua gioi han
     */
    void validateUserInvitationQuota(Integer companyId);

    /**
     * Kiem tra gioi han dung luong luu tru (Storage) cua doanh nghiep.
     * Bat buoc thuc hien truoc khi luu tep tin vat ly vao may chu hoac Cloud.
     * * @param companyId ID dinh danh cua Cong ty
     * @param fileSizeToUpload Kich thuoc tep tin du kien tai len (tinh bang Bytes)
     * @throws OverageException Neu dung luong tep tin vuot qua khoang trong con lai cua goi cuoc
     */
    void validateStorageQuota(Integer companyId, long fileSizeToUpload);
}