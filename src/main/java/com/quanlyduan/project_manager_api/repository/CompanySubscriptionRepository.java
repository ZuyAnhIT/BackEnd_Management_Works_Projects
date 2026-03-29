package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Integer> {
    /**
     * Lấy gói cước hiện tại của công ty.
     */
    Optional<CompanySubscription> findByCompany_Id(Integer companyId);
    // Tìm các gói theo Trạng thái VÀ Ngày hết hạn nhỏ hơn một mốc thời gian nào đó
    List<CompanySubscription> findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus status, LocalDateTime dateTime);

    // Tìm các gói cước theo trạng thái và có ngày hết hạn nằm trong khoảng thời gian [start, end]
    List<CompanySubscription> findByStatusAndCurrentPeriodEndBetween(
            SubscriptionStatus status, 
            LocalDateTime start, 
            LocalDateTime end
    );
    
}