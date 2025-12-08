package com.quanlyduan.project_manager_api.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogActivity {
    String action();      // VD: "CREATE", "UPDATE"
    String entityType();  // VD: "TASK", "PROJECT"
    String description() default "";
}
