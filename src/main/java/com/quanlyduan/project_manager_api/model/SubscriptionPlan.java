package com.quanlyduan.project_manager_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entity dinh nghia cac Goi cuoc (Subscription Plan) trong he thong.
 * Quy dinh han muc tai nguyen (Users, Storage, Projects) va gia ca cho tung cap do thue bao.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Ma dinh danh duy nhat cua goi (vi du: "PRO_MONTHLY", "ENTERPRISE"). */
    @Column(name = "plan_code", nullable = false, unique = true)
    private String planCode;

    /** Ten hien thi cua goi cuoc (vi du: "Goi Chuyen nghiep"). */
    @Column(nullable = false)
    private String name;

    /** Mo ta chi tiet ve cac dac quyen cua goi. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ======================================================
    // 2. CHINH SACH GIA (PRICING POLICY)
    // ======================================================
    
    /** Gia thue bao theo thang. */
    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;

    /** Gia thue bao theo nam (thuong co chiet khau). */
    @Column(name = "yearly_price", nullable = false)
    private BigDecimal yearlyPrice;

    // ======================================================
    // 3. HAN MUC TAI NGUYEN (QUOTA & LIMITS)
    // ======================================================
    
    /** So luong thanh vien toi da trong mot Cong ty. */
    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    /** So luong Workspace toi da duoc phep tao. */
    @Column(name = "max_workspaces", nullable = false)
    private Integer maxWorkspaces;

    /** So luong Project toi da duoc phep quan ly. */
    @Column(name = "max_projects", nullable = false)
    private Integer maxProjects;

    /** Dung luong luu tru toi da (tinh bang GB). */
    @Column(name = "max_storage_gb", nullable = false)
    private Integer maxStorageGb;

    /** * Danh sach cac tinh nang mo rong (luu duoi dang JSON).
     * Vi du: {"ai_assistant": true, "advanced_reports": true}.
     */
    @Column(name = "features", columnDefinition = "JSON")
    private String features;

    // ======================================================
    // 4. TRANG THAI & HIEN THI (STATE & DISPLAY)
    // ======================================================
    
    /** Cờ xac dinh goi cuoc co con dang kinh doanh hay khong. */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /** Thu tu uu tien hien thi tren bang gia (Pricing Table). */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    // ======================================================
    // 5. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public SubscriptionPlan() {
    }

    public SubscriptionPlan(Integer id, String planCode, String name, String description, 
                            BigDecimal monthlyPrice, BigDecimal yearlyPrice, Integer maxUsers, 
                            Integer maxWorkspaces, Integer maxProjects, Integer maxStorageGb, 
                            String features, Boolean isActive, Integer sortOrder, 
                            User createdBy, User updatedBy, LocalDateTime createdAt, 
                            LocalDateTime updatedAt) {
        this.id = id;
        this.planCode = planCode;
        this.name = name;
        this.description = description;
        this.monthlyPrice = monthlyPrice;
        this.yearlyPrice = yearlyPrice;
        this.maxUsers = maxUsers;
        this.maxWorkspaces = maxWorkspaces;
        this.maxProjects = maxProjects;
        this.maxStorageGb = maxStorageGb;
        this.features = features;
        this.isActive = isActive;
        this.sortOrder = sortOrder;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}