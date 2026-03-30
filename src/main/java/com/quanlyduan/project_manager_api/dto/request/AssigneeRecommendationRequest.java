package com.quanlyduan.project_manager_api.dto.request;

import java.util.List;

import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO chua thong tin yeu cau goi y nguoi thuc hien (Assignee) phu hop nhat cho mot cong viec.
 * Ho tro du lieu dau vao cho cac tinh nang AI hoac Smart Recommendation.
 */
@Getter
@Setter
public class AssigneeRecommendationRequest {

    // ======================================================
    // THONG TIN CONG VIEC (TASK DETAILS)
    // ======================================================

    /**
     * Tieu de cong viec can goi y.
     * Vi du: Fix loi thanh toan.
     */
    private String title;

    /**
     * Mo ta chi tiet noi dung hoac loi gap phai.
     * Cung cap ngu canh (context) chinh cho thuat toan phan tich.
     */
    private String description;

    /**
     * Loai cong viec can xu ly.
     * Ho tro AI phan loai (vi du: BUG thuong giao cho Dev hien tai, STORY giao cho Lead).
     */
    private TaskType taskType;

    /**
     * Danh sach cac the (tags) lien quan den chuyen mon hoac module he thong.
     * Vi du: Backend, Payment, Java.
     */
    private List<String> tags;

    /**
     * Diem uoc luong do phuc tap cua cong viec.
     * Du lieu nay giup thuat toan tinh toan tai cong viec hien tai cua nhan su, tranh qua tai.
     */
    private Integer storyPoints;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh ho tro Spring/Jackson map du lieu tu JSON.
     */
    public AssigneeRecommendationRequest() {
    }
}