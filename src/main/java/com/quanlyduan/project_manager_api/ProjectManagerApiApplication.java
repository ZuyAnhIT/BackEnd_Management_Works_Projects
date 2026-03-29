package com.quanlyduan.project_manager_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Lop khoi chay chinh (Entry Point) cua he thong Project Manager API.
 * - @SpringBootApplication: Cau hinh tu dong va quet thanh phan cho Spring Boot.
 * - @EnableAsync: Cho phep thuc thi cac tac vu bat dong bo (Background Tasks).
 * - @EnableScheduling: Cho phep thuc thi cac tac vu lap lich tu dong (Cron Jobs).
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ProjectManagerApiApplication {

    /**
     * Phuong thuc main de khoi chay ung dung.
     */
    public static void main(String[] args) {
        SpringApplication.run(ProjectManagerApiApplication.class, args);
    }

}