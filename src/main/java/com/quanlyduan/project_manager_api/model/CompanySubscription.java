package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity lien ket giua Cong ty va Goi cuoc (Subscription).
 * Quan ly thoi han su dung, trang thai thanh toan va chu ky gia han cua dich vu SaaS.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "company_subscriptions")
public class CompanySubscription {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Cong ty so huu goi dang ky nay. Su dung ManyToOne de luu lai lich su cac goi. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false) 
    private Company company;

    /** Goi cuoc (Plan) ma cong ty dang ap dung (vi du: FREE, PRO, ENTERPRISE). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    // ======================================================
    // 3. TRANG THAI & CHU KY (STATUS & PERIOD)
    // ======================================================

    /** * Trang thai hien tai cua goi dang ky.
     * Gia tri: TRIAL, ACTIVE, PAST_DUE (No cuoc), CANCELED, EXPIRED. 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status; 

    /** Thoi diem bat dau va ket thuc cua chu ky thanh toan hien tai. */
    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    /** * Co danh dau se huy goi khi het chu ky hien tai.
     * Dung de ho tro tinh nang "Huy dang ky nhung van dung den het thang".
     */
    @Column(name = "cancel_at_period_end")
    private Boolean cancelAtPeriodEnd;

    // ======================================================
    // 4. THONG TIN DUNG THU (TRIAL INFO)
    // ======================================================

    /** Thoi gian bat dau va ket thuc giai doan dung thu (Trial). */
    @Column(name = "trial_starts_at")
    private LocalDateTime trialStartsAt;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    // ======================================================
    // 5. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public CompanySubscription() {
    }

    public CompanySubscription(Integer id, Company company, SubscriptionPlan plan, 
                               SubscriptionStatus status, LocalDateTime currentPeriodStart, 
                               LocalDateTime currentPeriodEnd, Boolean cancelAtPeriodEnd, 
                               LocalDateTime trialStartsAt, LocalDateTime trialEndsAt, 
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.company = company;
        this.plan = plan;
        this.status = status;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
        this.trialStartsAt = trialStartsAt;
        this.trialEndsAt = trialEndsAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}