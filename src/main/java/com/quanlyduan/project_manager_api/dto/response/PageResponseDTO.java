// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/PageResponseDTO.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {

    private List<T> content;        // Danh sách dữ liệu
    private int pageNumber;         // Trang hiện tại (0, 1, 2...)
    private int pageSize;           // Số lượng phần tử/trang
    private long totalElements;     // Tổng số bản ghi trong DB
    private int totalPages;         // Tổng số trang
    private boolean last;           // Là trang cuối?
    private boolean first;          // Là trang đầu?

    /**
     * Constructor tiện ích chuyển đổi từ Spring Data Page
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