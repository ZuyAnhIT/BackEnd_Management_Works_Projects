package com.quanlyduan.project_manager_api.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de sap xep lai thu tu cac cot trang thai (Status Columns).
 * Thuong duoc su dung khi nguoi dung keo tha de thay doi vi tri cot tren bang cong viec (Board).
 */
@Getter
@Setter
public class ReorderStatusRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String LIST_EMPTY_MSG = "Ordered status IDs list must not be empty";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * Danh sach ID cua cac trang thai theo thu tu moi mong muon.
     * Vi du: [10, 5, 8] nghia la cot ID=10 se nam o vi tri dau tien (Index 0).
     * Bat buoc phai co it nhat mot ID de thuc hien hanh dong.
     */
    @NotEmpty(message = LIST_EMPTY_MSG)
    private List<Integer> orderedStatusIds;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu chuoi JSON.
     */
    public ReorderStatusRequest() {
    }
}