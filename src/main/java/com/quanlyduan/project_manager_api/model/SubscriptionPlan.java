package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ thông tin các Gói cước (Plans) của hệ thống SaaS.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "plan_code", nullable = false, unique = true)
    private String planCode; // VD: "FREE", "PRO", "ENTERPRISE"

    @Column(nullable = false)
    private String name;

    @Column(name = "monthly_price")
    private BigDecimal monthlyPrice;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_storage_gb")
    private Integer maxStorageGb;

    @Column(name = "is_active")
    private Boolean isActive;
}