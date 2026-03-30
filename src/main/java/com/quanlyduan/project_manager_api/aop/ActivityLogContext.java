package com.quanlyduan.project_manager_api.aop;

/**
 * Lop tien ich luu tru thong diep chi tiet cua hanh dong.
 * Su dung ThreadLocal de dam bao an toan trong moi truong da luong (thread-safe).
 */
public class ActivityLogContext {

    // Khai bao bien luu tru noi dung log cho luong hien tai
    private static final ThreadLocal<String> currentDetail = new ThreadLocal<>();

    // Constructor thu cong (private) de ngan chan viec khoi tao doi tuong
    private ActivityLogContext() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Ghi de thong diep chi tiet cho hanh dong
    public static void setDetail(String detail) {
        currentDetail.set(detail);
    }

    // Lay ra thong diep chi tiet dang luu trong luong
    public static String getDetail() {
        return currentDetail.get();
    }

    // Xoa du lieu sau khi dung xong de tranh ro ri bo nho (memory leak)
    public static void clear() {
        currentDetail.remove();
    }
}