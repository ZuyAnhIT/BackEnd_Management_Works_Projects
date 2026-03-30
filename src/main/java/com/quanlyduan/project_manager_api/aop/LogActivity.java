package com.quanlyduan.project_manager_api.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Chu thich dung de danh dau cac ham (method) can duoc ghi nhan lich su hoat dong.
 * ActivityLogAspect se quet cac ham co chu thich nay de tu dong luu log.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogActivity {

    /**
     * Ten hanh dong duoc thuc hien.
     * Vi du: "CREATE", "UPDATE", "DELETE", "INVITE"
     */
    String action();

    /**
     * Loai thuc the chiu tac dong cua hanh dong.
     * Vi du: "TASK", "PROJECT", "WORKSPACE"
     */
    String entityType();

    /**
     * Mo ta chi tiet mac dinh cho hanh dong (khong bat buoc).
     * Se duoc su dung neu ActivityLogContext khong cung cap chi tiet dong.
     */
    String description() default "";
}