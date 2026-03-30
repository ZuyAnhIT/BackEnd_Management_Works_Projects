package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi du lieu phan bo cong viec theo Trang thai (Status Distribution).
 * Duoc su dung de ve bieu do tron (Pie Chart) hoac thanh tien do (Progress Bar) 
 * nham nhan dien cac nut that co chai (Bottlenecks) trong du an.
 */
@Getter
@Setter
@Builder
public class StatusDistributionResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY INFO)
    // ======================================================

    /** ID dinh danh cua Trang thai (Status ID). */
    private Integer statusId;

    /** Ten hien thi cua trang thai (vi du: "To Do", "In Progress", "Done"). */
    private String statusName;

    /** Ma mau HEX dai dien de dong bo mau sac tren bieu do (vi du: "#3498db"). */
    private String color;

    // ======================================================
    // 2. CHI SO THONG KE (METRICS)
    // ======================================================

    /** Tong so luong Task hien dang nam o trang thai nay. */
    private Long taskCount;

    /** * Ty le phan tram cua trang thai nay so voi tong so Task.
     * Scale: 0.0 - 100.0.
     */
    private Double percentage;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the Deserialize JSON mot cach minh bach.
     */
    public StatusDistributionResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public StatusDistributionResponse(Integer statusId, String statusName, String color, 
                                      Long taskCount, Double percentage) {
        this.statusId = statusId;
        this.statusName = statusName;
        this.color = color;
        this.taskCount = taskCount;
        this.percentage = percentage;
    }
}