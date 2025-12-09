package com.quanlyduan.project_manager_api.aop;

/**
 * Class này dùng để lưu trữ thông điệp chi tiết của hành động (Ví dụ: "đổi tên từ A sang B")
 * để Aspect có thể đọc được và ghi vào Log.
 * Sử dụng ThreadLocal để đảm bảo an toàn trong môi trường đa luồng.
 */
public class ActivityLogContext {
    private static final ThreadLocal<String> currentDetail = new ThreadLocal<>();

    public static void setDetail(String detail) {
        currentDetail.set(detail);
    }

    public static String getDetail() {
        return currentDetail.get();
    }

    public static void clear() {
        currentDetail.remove();
    }
    
}