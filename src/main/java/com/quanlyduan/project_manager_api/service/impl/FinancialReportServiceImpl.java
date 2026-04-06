package com.quanlyduan.project_manager_api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.FinancialDashboardResponse;
import com.quanlyduan.project_manager_api.model.Transaction;
import com.quanlyduan.project_manager_api.model.common.enums.TransactionStatus;
import com.quanlyduan.project_manager_api.repository.TransactionRepository;
import com.quanlyduan.project_manager_api.service.FinancialReportService;

@Service
@Transactional(readOnly = true)
public class FinancialReportServiceImpl implements FinancialReportService {

    public static final int RECENT_TRANSACTIONS_LIMIT = 10;
    public static final int ROUNDING_SCALE_PERCENTAGE = 2;
    public static final int MATH_SCALE_DIVISION = 4;
    public static final double HUNDRED_PERCENT = 100.0;

    private final TransactionRepository transactionRepository;

    // Constructor thay the cho @Autowired
    public FinancialReportServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    public FinancialDashboardResponse getFinancialOverview(Integer targetYear, Integer targetMonth) {
        LocalDate currentDate = LocalDate.now();
        int year = (targetYear != null) ? targetYear : currentDate.getYear();
        int month = (targetMonth != null) ? targetMonth : currentDate.getMonthValue();

        // 1. Tinh toan moc thoi gian Thang Hien Tai (Current Month)
        LocalDate startOfCurrentMonth = LocalDate.of(year, month, 1);
        LocalDate endOfCurrentMonth = startOfCurrentMonth.withDayOfMonth(startOfCurrentMonth.lengthOfMonth());
        LocalDateTime currentMonthStart = startOfCurrentMonth.atStartOfDay();
        LocalDateTime currentMonthEnd = endOfCurrentMonth.atTime(LocalTime.MAX);

        // 2. Tinh toan moc thoi gian Thang Truoc (Previous Month)
        LocalDate startOfPrevMonth = startOfCurrentMonth.minusMonths(1);
        LocalDate endOfPrevMonth = startOfPrevMonth.withDayOfMonth(startOfPrevMonth.lengthOfMonth());
        LocalDateTime prevMonthStart = startOfPrevMonth.atStartOfDay();
        LocalDateTime prevMonthEnd = endOfPrevMonth.atTime(LocalTime.MAX);

        // 3. Lay du lieu Doanh thu (MRR)
        BigDecimal currentMrr = transactionRepository.sumRevenueByDateRangeAndStatus(
                currentMonthStart, currentMonthEnd, TransactionStatus.SUCCESS);
        BigDecimal previousMrr = transactionRepository.sumRevenueByDateRangeAndStatus(
                prevMonthStart, prevMonthEnd, TransactionStatus.SUCCESS);

        FinancialDashboardResponse.MrrMetric mrrMetric = calculateMrrGrowth(currentMrr, previousMrr);

        // 4. Lay du lieu Phan bo doanh thu theo Goi cuoc
        List<Object[]> rawPlanData = transactionRepository.groupRevenueByPlanAndDateRange(
                currentMonthStart, currentMonthEnd, TransactionStatus.SUCCESS);
        List<FinancialDashboardResponse.RevenueByPlan> revenueByPlans = mapToRevenueByPlan(rawPlanData, currentMrr);

        // 5. Lay 10 Giao dich gan nhat
        Pageable limit = PageRequest.of(0, RECENT_TRANSACTIONS_LIMIT);
        List<Transaction> recentTransactions = transactionRepository.findRecentTransactions(limit);
        
        return FinancialDashboardResponse.builder()
                .mrr(mrrMetric)
                .revenueByPlans(revenueByPlans)
                .recentTransactions(mapToTransactionRecords(recentTransactions))
                .build();
    }

    // --- CAC HAM PRIVATE HO TRO (MAPPING VA TINH TOAN) ---

    private FinancialDashboardResponse.MrrMetric calculateMrrGrowth(BigDecimal current, BigDecimal previous) {
        double growthPercentage = 0.0;
        boolean isPositive = true;

        if (previous.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = current.subtract(previous);
            BigDecimal ratio = diff.divide(previous, MATH_SCALE_DIVISION, RoundingMode.HALF_UP);
            growthPercentage = ratio.multiply(BigDecimal.valueOf(HUNDRED_PERCENT)).doubleValue();
            isPositive = diff.compareTo(BigDecimal.ZERO) >= 0;
        } else if (current.compareTo(BigDecimal.ZERO) > 0) {
            growthPercentage = HUNDRED_PERCENT;
        }

        return FinancialDashboardResponse.MrrMetric.builder()
                .currentMonthRevenue(current)
                .previousMonthRevenue(previous)
                .growthPercentage(growthPercentage)
                .isPositiveGrowth(isPositive)
                .build();
    }

    private List<FinancialDashboardResponse.RevenueByPlan> mapToRevenueByPlan(List<Object[]> rawData, BigDecimal totalRevenue) {
        return rawData.stream().map(row -> {
            String name = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            
            double percentage = 0.0;
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.divide(totalRevenue, MATH_SCALE_DIVISION, RoundingMode.HALF_UP)
                                   .multiply(BigDecimal.valueOf(HUNDRED_PERCENT))
                                   .doubleValue();
            }

            return FinancialDashboardResponse.RevenueByPlan.builder()
                    .planName(name)
                    .totalRevenue(amount)
                    .percentage(percentage)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<FinancialDashboardResponse.TransactionRecord> mapToTransactionRecords(List<Transaction> transactions) {
        return transactions.stream()
                .map(t -> FinancialDashboardResponse.TransactionRecord.builder()
                        .transactionCode(t.getTransactionCode())
                        .companyName(t.getCompany().getName())
                        .planName(t.getPlan().getName())
                        .amount(t.getAmount())
                        .paidAt(t.getPaidAt() != null ? t.getPaidAt() : t.getCreatedAt())
                        .status(t.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }
}