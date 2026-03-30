package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat thong tin cua mot Trang thai (Cot tren Board).
 * Ho tro co che Partial Update: Chi nhung truong co gia tri (khong null) moi duoc cap nhat vao he thong.
 */
@Getter
@Setter
public class UpdateStatusRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int NAME_MAX_SIZE = 100;
    public static final String NAME_SIZE_MSG = "Status name must not exceed " + NAME_MAX_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN HIEN THI (UI INFO)
    // ======================================================

    /**
     * Ten moi cua trang thai (vi du: To Do, Doing, Done).
     */
    @Size(max = NAME_MAX_SIZE, message = NAME_SIZE_MSG)
    private String name;

    /**
     * Ma mau HEX moi hien thi cho cot trang thai (vi du: #e74c3c).
     */
    private String color; 

    // ======================================================
    // 2. LOGIC HE THONG (SYSTEM LOGIC)
    // ======================================================

    /**
     * Co danh dau trang thai "Hoan thanh".
     * Neu true: Cac Task duoc keo vao cot nay se tu dong duoc danh dau la da hoan thanh.
     */
    private Boolean isCompletedStatus; 

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public UpdateStatusRequest() {
    }
}