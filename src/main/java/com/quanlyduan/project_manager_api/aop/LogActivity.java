package com.quanlyduan.project_manager_api.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Chú thích dùng để đánh dấu các hàm (method) cần được ghi nhận lịch sử hoạt động.
 * ActivityLogAspect sẽ quét các hàm có chú thích này để tự động lưu log.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogActivity {

    /**
     * Tên hành động được thực hiện.
     * Ví dụ: "CREATE", "UPDATE", "DELETE", "INVITE"
     */
    String action();

    /**
     * Loại thực thể chịu tác động của hành động.
     * Ví dụ: "TASK", "PROJECT", "WORKSPACE"
     */
    String entityType();

    /**
     * Mô tả chi tiết mặc định cho hành động (không bắt buộc).
     * Sẽ được sử dụng nếu ActivityLogContext không cung cấp chi tiết động.
     */
    String description() default "";
}