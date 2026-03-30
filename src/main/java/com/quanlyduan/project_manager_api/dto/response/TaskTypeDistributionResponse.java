package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi du lieu phan bo theo Loai cong viec (Task Type Distribution).
 * Dung de hien thi bieu do tron (Pie Chart) nham phan tich ty trong giua 
 * Story, Bug, Task va cac loai khac trong du an.
 */
@Getter
@Setter
@Builder
public class TaskTypeDistributionResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY INFO)
    // ======================================================
    
    /** Ten hien thi cua loai cong viec (vi du: "Tinh nang", "Loi he thong"). */
    private String typeName; 

    /** Ma Code cua Enum (vi du: STORY, BUG, TASK, IMPROVEMENT). */
    private String typeCode; 

    /** Ma mau HEX dai dien de dong bo mau sac tren bieu do (vi du: "#2ecc71"). */
    private String color;

    // ======================================================
    // 2. CHI SO THONG KE (METRICS)
    // ======================================================

    /** Tong so luong Task thuoc loai nay hien co trong Project/Sprint. */
    private Long taskCount; 

    /** * Ty le phan tram so voi tong so Task.
     * Scale: 0.0 - 100.0. 
     */
    private Double percentage;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao tinh tuong thich cao voi cac thu vien Deserialize JSON.
     */
    public TaskTypeDistributionResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public TaskTypeDistributionResponse(String typeName, String typeCode, String color, 
                                        Long taskCount, Double percentage) {
        this.typeName = typeName;
        this.typeCode = typeCode;
        this.color = color;
        this.taskCount = taskCount;
        this.percentage = percentage;
    }
}