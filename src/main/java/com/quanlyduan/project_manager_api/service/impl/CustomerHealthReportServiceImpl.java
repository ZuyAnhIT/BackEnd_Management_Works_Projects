package com.quanlyduan.project_manager_api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.CustomerHealthDashboardResponse;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.CompanySubscriptionRepository;
import com.quanlyduan.project_manager_api.service.CustomerHealthReportService;

@Service
@Transactional(readOnly = true)
public class CustomerHealthReportServiceImpl implements CustomerHealthReportService {

    public static final int MATH_SCALE_PERCENTAGE = 2;
    public static final double PERCENT_MULTIPLIER = 100.0;

    private final CompanyRepository companyRepository;
    private final CompanySubscriptionRepository subscriptionRepository;

    public CustomerHealthReportServiceImpl(CompanyRepository companyRepository, 
                                           CompanySubscriptionRepository subscriptionRepository) {
        this.companyRepository = companyRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public CustomerHealthDashboardResponse getCustomerHealthOverview(Integer targetYear, Integer targetMonth) {
        LocalDate referenceDate = LocalDate.now();
        int year = (targetYear != null) ? targetYear : referenceDate.getYear();
        int month = (targetMonth != null) ? targetMonth : referenceDate.getMonthValue();

        // 1. Tinh toan thoi gian cho thang hien tai
        LocalDate startOfCurrent = LocalDate.of(year, month, 1);
        LocalDateTime currentStart = startOfCurrent.atStartOfDay();
        LocalDateTime currentEnd = startOfCurrent.withDayOfMonth(startOfCurrent.lengthOfMonth()).atTime(LocalTime.MAX);

        // 2. Tinh toan thoi gian cho thang truoc
        LocalDate startOfPrevious = startOfCurrent.minusMonths(1);
        LocalDateTime previousEnd = startOfPrevious.withDayOfMonth(startOfPrevious.lengthOfMonth()).atTime(LocalTime.MAX);

        // --- A. ACTIVE TENANTS ---
        // Lay snapshot so luong khach hang active o cuoi thang nay so voi cuoi thang truoc
        long currentActiveCount = subscriptionRepository.countActiveTenantsAtDate(currentEnd);
        long previousActiveCount = subscriptionRepository.countActiveTenantsAtDate(previousEnd);
        CustomerHealthDashboardResponse.ActiveTenantMetric activeMetric = calculateActiveGrowth(currentActiveCount, previousActiveCount);

        // --- B. NEW VS CHURN ---
        long newTenants = companyRepository.countNewCompaniesByDateRange(currentStart, currentEnd);
        long churnedTenants = subscriptionRepository.countChurnedTenantsByDateRange(currentStart, currentEnd);
        
        CustomerHealthDashboardResponse.NewVsChurnMetric retentionMetric = CustomerHealthDashboardResponse.NewVsChurnMetric.builder()
                .newTenants(newTenants)
                .churnedTenants(churnedTenants)
                .netRetention(newTenants - churnedTenants)
                .build();

        // --- C. TENANTS BY PLAN ---
        List<Object[]> rawPlanData = subscriptionRepository.groupActiveTenantsByPlan();
        List<CustomerHealthDashboardResponse.TenantByPlan> planMetrics = mapToPlanMetrics(rawPlanData, currentActiveCount);

        return CustomerHealthDashboardResponse.builder()
                .activeTenants(activeMetric)
                .newVsChurn(retentionMetric)
                .tenantsByPlan(planMetrics)
                .build();
    }

    // --- PRIVATE HELPERS ---

    private CustomerHealthDashboardResponse.ActiveTenantMetric calculateActiveGrowth(long current, long previous) {
        double growth = 0.0;
        boolean isPositive = true;

        if (previous > 0) {
            double difference = (double) (current - previous);
            growth = (difference / previous) * PERCENT_MULTIPLIER;
            // Lam tron 2 chu so thap phan
            growth = BigDecimal.valueOf(growth).setScale(MATH_SCALE_PERCENTAGE, RoundingMode.HALF_UP).doubleValue();
            isPositive = difference >= 0;
        } else if (current > 0) {
            growth = PERCENT_MULTIPLIER; 
        }

        return CustomerHealthDashboardResponse.ActiveTenantMetric.builder()
                .currentActiveCount(current)
                .previousActiveCount(previous)
                .growthPercentage(growth)
                .isPositiveGrowth(isPositive)
                .build();
    }

    private List<CustomerHealthDashboardResponse.TenantByPlan> mapToPlanMetrics(List<Object[]> rawData, long totalActive) {
        return rawData.stream().map(row -> {
            String planName = (String) row[0];
            Long count = (Long) row[1];
            
            double ratio = 0.0;
            if (totalActive > 0) {
                ratio = ((double) count / totalActive) * PERCENT_MULTIPLIER;
                ratio = BigDecimal.valueOf(ratio).setScale(MATH_SCALE_PERCENTAGE, RoundingMode.HALF_UP).doubleValue();
            }

            return CustomerHealthDashboardResponse.TenantByPlan.builder()
                    .planName(planName)
                    .tenantCount(count)
                    .percentage(ratio)
                    .build();
        }).collect(Collectors.toList());
    }
}

