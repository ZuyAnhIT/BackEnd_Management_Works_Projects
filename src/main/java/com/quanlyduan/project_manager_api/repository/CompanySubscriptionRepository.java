package com.quanlyduan.project_manager_api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;

/**
 * Kho luu tru du lieu quan ly goi cuoc cua cong ty (Company Subscription).
 * Cung cap cac phuong thuc truy van thoi han su dung de phuc vu logic thanh toan va lap lich.
 */
@Repository
public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Integer> {

    /**
     * Truy van thong tin goi cuoc hien tai cua mot cong ty dua tren ID.
     * @param companyId ID cua cong ty can kiem tra.
     * @return Ket qua duoi dang Optional chua thong tin goi cuoc.
     */
    Optional<CompanySubscription> findByCompany_Id(Integer companyId);

    /**
     * Tim kiem danh sach cac goi cuoc co ngay het han truoc mot moc thoi gian chi dinh.
     * Thuong dung trong cac tac vu tu dong (Cron Job) de quet cac goi cuoc da qua han.
     * @param status Trang thai cua goi cuoc (vi du: ACTIVE, PAST_DUE).
     * @param dateTime Moc thoi gian de so sanh (thuong la thoi diem hien tai).
     */
    List<CompanySubscription> findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus status, LocalDateTime dateTime);

    /**
     * Tim kiem danh sach cac goi cuoc se het han trong mot khoang thoi gian nhat dinh.
     * Dung de xac dinh cac goi cuoc sap het han de gui email canh bao cho khach hang.
     * @param status Trang thai cua goi cuoc.
     * @param start Thoi diem bat dau khoang thoi gian loc.
     * @param end Thoi diem ket thuc khoang thoi gian loc.
     */
    List<CompanySubscription> findByStatusAndCurrentPeriodEndBetween(
            SubscriptionStatus status, 
            LocalDateTime start, 
            LocalDateTime end
    );

    String COUNT_ACTIVE_TENANTS_BY_DATE = 
        "SELECT COUNT(DISTINCT s.company.id) FROM CompanySubscription s " +
        "WHERE s.company.status = 'ACTIVE' AND s.status = 'ACTIVE' " +
        "AND s.currentPeriodStart <= :targetDate AND s.currentPeriodEnd >= :targetDate";

    String COUNT_CHURNED_TENANTS_BY_DATE_RANGE = 
        "SELECT COUNT(DISTINCT s.company.id) FROM CompanySubscription s " +
        "WHERE s.status IN ('PAST_DUE', 'CANCELLED', 'EXPIRED') " +
        "AND s.currentPeriodEnd >= :startDate AND s.currentPeriodEnd <= :endDate";

    String GROUP_TENANTS_BY_PLAN = 
        "SELECT s.plan.name, COUNT(DISTINCT s.company.id) FROM CompanySubscription s " +
        "WHERE s.company.status = 'ACTIVE' AND s.status = 'ACTIVE' " +
        "GROUP BY s.plan.name ORDER BY COUNT(DISTINCT s.company.id) DESC";

    /**
     * Dem tong so cong ty (Tenants) dang hoat dong tai mot thoi diem nhat dinh.
     */
    @Query(COUNT_ACTIVE_TENANTS_BY_DATE)
    long countActiveTenantsAtDate(@Param("targetDate") LocalDateTime targetDate);

    /**
     * Dem so cong ty roi bo (Churn) nghia la goi cuoc cua ho het han/that bai trong khoang thoi gian nay.
     */
    @Query(COUNT_CHURNED_TENANTS_BY_DATE_RANGE)
    long countChurnedTenantsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Gom nhom so luong khach hang theo tung goi cuoc dang su dung.
     */
    @Query(GROUP_TENANTS_BY_PLAN)
    List<Object[]> groupActiveTenantsByPlan();
}