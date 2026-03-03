// File: CompanySubscriptionRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CompanySubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Integer> {
}