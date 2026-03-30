package com.quanlyduan.project_manager_api.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO chua cac tieu chi de loc danh sach The (Tag).
 * Duoc su dung de truyen tham so tim kiem tu Controller xuong Service/Repository qua Specification.
 */
@Getter
@Setter
public class TagFilterRequest {

    // ======================================================
    // 1. TIM KIEM TU KHOA (TEXT SEARCH)
    // ======================================================
    
    /**
     * Tu khoa tim kiem chung.
     * Logic: Tim kiem gan dung (LIKE %keyword%) trong Ten the hoac Mo ta.
     */
    private String keyword; 

    // ======================================================
    // 2. LOC THEO THUOC TINH (ATTRIBUTE FILTER)
    // ======================================================

    /**
     * Danh sach cac ten the cu the muon loc.
     * Logic: Su dung toan tu IN de loc chinh xac theo tap hop (vi du: tu checkbox).
     */
    private List<String> names; 

    /**
     * ID cua nguoi dung da tao the.
     * Logic: Loc chinh xac theo ID nguoi dung (Equal).
     */
    private Integer createdById;

    // ======================================================
    // 3. LOC THEO THOI GIAN (DATE RANGE FILTER)
    // ======================================================

    /**
     * Thoi diem bat dau (Tu ngay).
     * Logic: Tim cac the duoc tao SAU hoac BANG thoi diem nay (>=).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;

    /**
     * Thoi diem ket thuc (Den ngay).
     * Logic: Tim cac the duoc tao TRUOC hoac BANG thoi diem nay (<=).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Spring co the bind Query Parameters vao Object.
     */
    public TagFilterRequest() {
    }
}