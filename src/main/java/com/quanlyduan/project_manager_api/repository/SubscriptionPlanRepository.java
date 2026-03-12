// File: SubscriptionPlanRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.SubscriptionPlan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer>, JpaSpecificationExecutor<SubscriptionPlan> {
    // Tìm gói cước theo mã (VD: "FREE")
    Optional<SubscriptionPlan> findByPlanCode(String planCode);
    boolean existsByPlanCode(String planCode);

    /**
     * Kiểm tra xem mã planCode đã tồn tại ở một gói cước KHÁC (khác với id truyền vào) hay chưa.
     * Dùng cho luồng Update.
     */
    boolean existsByPlanCodeAndIdNot(String planCode, Integer id);

    Page<SubscriptionPlan> findByIsActiveTrue(Pageable pageable);
    
}