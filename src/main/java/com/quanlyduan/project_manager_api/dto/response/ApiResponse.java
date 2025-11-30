// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ApiResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lớp bao đóng (Wrapper) chuẩn cho mọi phản hồi từ API.
 * Giúp Frontend luôn nhận được một cấu trúc dữ liệu nhất quán: { success, message, data }.
 * @param <T> Kiểu dữ liệu của payload (ví dụ: UserResponse, List<Project>...).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    // Trạng thái thành công của request (true: Thành công, false: Thất bại)
    private boolean success;

    // Thông báo mô tả kết quả (ví dụ: "Operation successful", "Validation failed")
    private String message;

    // Dữ liệu chính trả về (Payload). Có thể là null nếu có lỗi hoặc không có dữ liệu.
    private T data;

    // ========================================================================
    // STATIC FACTORY METHODS (Hàm tiện ích để tạo đối tượng nhanh)
    // ========================================================================

    /**
     * Tạo phản hồi thành công (Success Response).
     * @param message Thông báo thành công.
     * @param data Dữ liệu trả về.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Tạo phản hồi lỗi cơ bản (Error Response).
     * Dữ liệu data sẽ là null.
     * @param message Thông báo lỗi.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
    
    /**
     * Tạo phản hồi lỗi kèm dữ liệu chi tiết (Detailed Error Response).
     * Thường dùng để trả về danh sách lỗi Validation (ví dụ: password quá ngắn, email sai định dạng...).
     * @param message Thông báo lỗi chung.
     * @param data Chi tiết lỗi (ví dụ: Map<String, String> errors).
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}