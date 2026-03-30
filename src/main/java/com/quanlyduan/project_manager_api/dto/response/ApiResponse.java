package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lop bao dong (Wrapper) chuan cho moi phan hoi tu API.
 * Giup Frontend luon nhan duoc mot cau truc du lieu nhat quan: { success, message, data }.
 * * @param <T> Kieu du lieu cua payload (vi du: UserResponse, List<Project>...).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String DEFAULT_SUCCESS_MSG = "Operation successful";
    public static final String DEFAULT_ERROR_MSG = "An error occurred";

    // ======================================================
    // CAU TRUC DU LIEU (RESPONSE FIELDS)
    // ======================================================

    /** Trang thai thanh cong cua request (true: Thanh cong, false: That bai). */
    private boolean success;

    /** Thong bao mo ta ket qua (vi du: "Validation failed", "User created"). */
    private String message;

    /** * Du lieu chinh tra ve (Payload). 
     * Co the la null neu co loi hoac API chi thuc hien lenh ma khong can tra du lieu. 
     */
    private T data;

    // ======================================================
    // STATIC FACTORY METHODS (UTILITIES)
    // ======================================================

    /**
     * Tao phan hoi thanh cong kem du lieu.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message != null ? message : DEFAULT_SUCCESS_MSG)
                .data(data)
                .build();
    }

    /**
     * Tao phan hoi thanh cong khong kem du lieu.
     */
    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    /**
     * Tao phan hoi loi co ban (Data se la null).
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message != null ? message : DEFAULT_ERROR_MSG)
                .data(null)
                .build();
    }
    
    /**
     * Tao phan hoi loi kem du lieu chi tiet (Dung cho loi Validation).
     * @param data Chi tiet cac truong bi loi (vi du: Map<String, String>).
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message != null ? message : DEFAULT_ERROR_MSG)
                .data(data)
                .build();
    }
}