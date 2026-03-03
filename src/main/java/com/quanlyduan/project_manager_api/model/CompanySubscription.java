package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
/**
 * Entity liên kết giữa Công ty và Gói cước họ đang sử dụng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_subscriptions")
public class CompanySubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status; // Enum: TRIAL, ACTIVE, EXPIRED...

    @Column(name = "trial_starts_at")
    private LocalDateTime trialStartsAt;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;
}