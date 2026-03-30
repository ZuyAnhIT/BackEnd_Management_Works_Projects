package com.quanlyduan.project_manager_api.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat Epic cho mot Cong viec (Task).
 * Thuong dung trong cac thao tac keo tha Task vao/ra khoi Panel Epic tren giao dien Board.
 */
@Getter
@Setter
public class UpdateTaskEpicRequest {

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * ID cua Epic muc tieu muon gan cho Task.
     * - Neu truyen ID: He thong se gan Task vao Epic do.
     * - Neu truyen NULL: He thong se go Task khoi Epic hien tai (Unassign).
     */
    private Integer epicId;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public UpdateTaskEpicRequest() {
    }
}