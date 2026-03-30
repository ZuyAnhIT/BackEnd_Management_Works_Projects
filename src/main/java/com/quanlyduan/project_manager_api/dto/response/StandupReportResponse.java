package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi bao cao hop dung (Standup Report).
 * Tong hop tinh hinh cong viec cua cac thanh vien trong 24h qua va ke hoach tiep theo.
 */
@Getter
@Setter
@Builder
public class StandupReportResponse {

    // ======================================================
    // 1. THONG TIN CHUNG (REPORT HEADER)
    // ======================================================

    /** Ten Sprint hien tai (vi du: "Sprint 2 - UI/UX Refactor"). */
    private String sprintName;

    /** Ngay thuc hien bao cao Standup. */
    private LocalDate reportDate;

    // ======================================================
    // 2. CHI TIET CAP NHAT TU THANH VIEN (MEMBER UPDATES)
    // ======================================================

    /** Danh sach cac ban tin cap nhat tu tung thanh vien trong Team. */
    private List<MemberUpdate> members;

    // ======================================================
    // INNER CLASS: CHI TIET CAP NHAT (RULE 8)
    // ======================================================

    @Getter
    @Setter
    @Builder
    public static class MemberUpdate {
        
        /** Ho ten va Anh dai dien cua thanh vien. */
        private String fullName;
        private String avatarUrl;
        
        /** * Nhung cong viec da hoan thanh trong 24h qua. 
         * Tuong ung voi cau hoi: "What did you do yesterday?"
         */
        private List<String> completedTasks;
        
        /** * Nhung cong viec dang thuc hien (In Progress). 
         * Tuong ung voi cau hoi: "What will you do today?"
         */
        private List<String> inProgressTasks;
        
        /** * Nhung cong viec du kien se thuc hien tiep theo (To Do).
         * Giup team nhan dien som cac dau viec sap toi.
         */
        private List<String> todoTasks;

        // Constructor viet tay cho Inner Class
        public MemberUpdate() {}

        public MemberUpdate(String fullName, String avatarUrl, List<String> completedTasks, 
                            List<String> inProgressTasks, List<String> todoTasks) {
            this.fullName = fullName;
            this.avatarUrl = avatarUrl;
            this.completedTasks = completedTasks;
            this.inProgressTasks = inProgressTasks;
            this.todoTasks = todoTasks;
        }
    }

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public StandupReportResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public StandupReportResponse(String sprintName, LocalDate reportDate, List<MemberUpdate> members) {
        this.sprintName = sprintName;
        this.reportDate = reportDate;
        this.members = members;
    }
}