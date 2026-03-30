package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat NHANH trang thai cua SubTask (Cong viec phu).
 * Thuong dung cho cac thao tac Quick Action nhu Check-box hoac Drag-and-drop tren UI.
 * Giup giam tai du lieu truyen tai so voi viec dung UpdateSubTaskRequest day du.
 */
@Getter
@Setter
public class UpdateSubTaskStatusRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String STATUS_NULL_MSG = "Subtask status must not be null";

    // ======================================================
    // THONG TIN TRANG THAI (STATUS DATA)
    // ======================================================

    /**
     * Trang thai moi muon ap dung cho SubTask (Enum: TO_DO, IN_PROGRESS, DONE).
     * Bat buoc phai co de backend thuc hien logic cap nhat trang thai tuong ung.
     */
    @NotNull(message = STATUS_NULL_MSG)
    private SubTaskStatus status;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public UpdateSubTaskStatusRequest() {
    }
}