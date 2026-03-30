package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat trang thai vong doi cua Du an (Project Lifecycle).
 * Dung de chuyen doi trang thai tong the nhu: NEW -> IN_PROGRESS -> COMPLETED hoac CANCELLED.
 */
@Getter
@Setter
public class UpdateProjectStatusRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String STATUS_NULL_MSG = "New project status must not be null";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * Trang thai moi muon ap dung cho toan bo du an.
     * Bat buoc phai la mot gia tri hop le trong Enum ProjectStatus.
     */
    @NotNull(message = STATUS_NULL_MSG)
    private ProjectStatus newStatus;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring/Jackson co the khoi tao doi tuong tu JSON.
     */
    public UpdateProjectStatusRequest() {
    }
}