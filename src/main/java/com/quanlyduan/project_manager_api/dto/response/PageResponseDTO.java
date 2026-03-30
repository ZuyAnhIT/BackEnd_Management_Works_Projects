package com.quanlyduan.project_manager_api.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Generic DTO cho cac phan hoi phan trang (Paginated Responses).
 * Chuan hoa cau truc dau ra cho toan bo cac API danh sach trong he thong.
 * @param <T> Kieu du lieu cua phan tu (vi du: UserResponse, TaskResponse...).
 */
@Getter
@Setter
@Builder
public class PageResponseDTO<T> {

    // ======================================================
    // 1. DU LIEU CHINH (CORE DATA)
    // ======================================================

    /** Danh sach du lieu cua trang hien tai (Payload). */
    private List<T> content;

    // ======================================================
    // 2. CHI SO PHAN TRANG (PAGINATION METRICS)
    // ======================================================

    /** Chi so trang hien tai (0-based). */
    private int pageNumber;

    /** So luong phan tu toi da tren mot trang (Page Size). */
    private int pageSize;

    /** Tong so ban ghi co trong Database thoa man dieu kien loc. */
    private long totalElements;

    /** Tong so trang duoc tinh toan (totalElements / pageSize). */
    private int totalPages;

    // ======================================================
    // 3. CO DIEU HUONG (NAVIGATION FLAGS)
    // ======================================================

    /** Co danh dau trang cuoi cung -> Frontend an nut "Next". */
    private boolean last;

    /** Co danh dau trang dau tien -> Frontend an nut "Previous". */
    private boolean first;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public PageResponseDTO() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public PageResponseDTO(List<T> content, int pageNumber, int pageSize, 
                           long totalElements, int totalPages, boolean last, boolean first) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
        this.first = first;
    }

    /**
     * Constructor tien ich giup mapping nhanh tu doi tuong Page cua Spring Data JPA.
     * @param page Doi tuong Page tra ve tu Repository/Service.
     */
    public PageResponseDTO(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.first = page.isFirst();
    }
}