package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CompanySubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Integer> {
    /**
     * Lấy gói cước hiện tại của công ty.
     */
    Optional<CompanySubscription> findByCompany_Id(Integer companyId);
}