package com.quanlyduan.project_manager_api.aop;

/**
 * Lớp tiện ích lưu trữ thông điệp chi tiết của hành động.
 * Sử dụng ThreadLocal để đảm bảo an toàn trong môi trường đa luồng (thread-safe).
 */
public class ActivityLogContext {

    // Khai báo biến lưu trữ nội dung log cho luồng hiện tại
    private static final ThreadLocal<String> currentDetail = new ThreadLocal<>();

    // Constructor thủ công (private) để ngăn chặn việc khởi tạo đối tượng cho class tiện ích
    private ActivityLogContext() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Ghi đè thông điệp chi tiết cho hành động
    public static void setDetail(String detail) {
        currentDetail.set(detail);
    }

    // Lấy ra thông điệp chi tiết đang lưu trong luồng
    public static String getDetail() {
        return currentDetail.get();
    }

    // Xóa dữ liệu sau khi dùng xong để tránh rò rỉ bộ nhớ (memory leak)
    public static void clear() {
        currentDetail.remove();
    }
}