// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/PageResponseDTO.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic DTO for paginated responses.
 * Standardizes the output format for all list APIs with pagination.
 * * DTO chuẩn dùng để trả về dữ liệu phân trang cho toàn bộ hệ thống.
 * Sử dụng Generic <T> để có thể chứa bất kỳ loại đối tượng nào (User, Task, Project...).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {

    // The actual list of data for the current page.
    // VN: Danh sách dữ liệu chính của trang hiện tại (Payload).
    private List<T> content;

    // Current page index (0-based).
    // VN: Chỉ số trang hiện tại (Bắt đầu từ 0). Frontend cần +1 nếu muốn hiển thị cho người dùng.
    private int pageNumber;

    // Number of items per page.
    // VN: Kích thước trang (Số lượng phần tử tối đa trên 1 trang).
    private int pageSize;

    // Total number of items across all pages.
    // VN: Tổng số bản ghi có trong cơ sở dữ liệu (thỏa mãn điều kiện lọc).
    private long totalElements;

    // Total number of pages.
    // VN: Tổng số trang được tính toán dựa trên totalElements và pageSize.
    private int totalPages;

    // Indicates if this is the last page.
    // VN: Cờ đánh dấu: true nếu là trang cuối cùng -> Frontend ẩn nút "Next".
    private boolean last;

    // Indicates if this is the first page.
    // VN: Cờ đánh dấu: true nếu là trang đầu tiên -> Frontend ẩn nút "Previous".
    private boolean first;

    /**
     * Utility constructor to map from Spring Data's Page object to this DTO.
     * VN: Constructor tiện ích giúp chuyển đổi nhanh từ đối tượng Page (của Spring JPA) sang DTO này.
     * @param page Đối tượng Page trả về từ Repository.
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