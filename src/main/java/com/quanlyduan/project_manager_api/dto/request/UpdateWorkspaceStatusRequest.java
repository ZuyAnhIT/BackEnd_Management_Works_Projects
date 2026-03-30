package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat trang thai vong doi cua Khong gian lam viec (Workspace).
 * Duoc su dung cho cac hanh dong nhu: Kich hoat (ACTIVE), Luu tru (ARCHIVED) hoac Danh dau xoa (DELETED).
 */
@Getter
@Setter
public class UpdateWorkspaceStatusRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String STATUS_NULL_MSG = "New workspace status must not be null";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * Trang thai moi muon ap dung cho Khong gian lam viec.
     * Bat buoc phai la mot gia tri hop le trong Enum WorkspaceStatus.
     */
    @NotNull(message = STATUS_NULL_MSG)
    private WorkspaceStatus newStatus;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu chuoi JSON.
     */
    public UpdateWorkspaceStatusRequest() {
    }
}