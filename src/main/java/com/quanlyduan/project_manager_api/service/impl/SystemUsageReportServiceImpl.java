package com.quanlyduan.project_manager_api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.SystemUsageDashboardResponse;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.service.SystemUsageReportService;

@Service
@Transactional(readOnly = true)
public class SystemUsageReportServiceImpl implements SystemUsageReportService {

    // Define standard constants
    public static final int MATH_SCALE_PERCENTAGE = 2;
    public static final double PERCENT_MULTIPLIER = 100.0;
    
    // Mock server capacity: 1000 GB (1000L * 1024 * 1024 * 1024)
    public static final long SERVER_TOTAL_CAPACITY_BYTES = 1073741824000L; 

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    // Manual constructor injection
    public SystemUsageReportServiceImpl(UserRepository userRepository,
                                        CompanyRepository companyRepository,
                                        ProjectRepository projectRepository,
                                        TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    // --- MAIN BUSINESS LOGIC ---

    @Override
    public SystemUsageDashboardResponse getSystemUsageOverview(Integer targetYear, Integer targetMonth) {
        LocalDate referenceDate = LocalDate.now();
        int year = (targetYear != null) ? targetYear : referenceDate.getYear();
        int month = (targetMonth != null) ? targetMonth : referenceDate.getMonthValue();

        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDateTime periodStart = startOfMonth.atStartOfDay();
        LocalDateTime periodEnd = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth()).atTime(LocalTime.MAX);

        // 1. Total Active Users
        long totalUsers = userRepository.countTotalUsers();

        // 2. Storage Usage Calculation
        long totalUsedStorage = companyRepository.sumTotalStorageUsed();
        SystemUsageDashboardResponse.StorageMetric storageMetric = calculateStorageMetrics(totalUsedStorage);

        // 3. Engagement Stats (New Projects & Tasks in the specified month)
        long newProjects = projectRepository.countNewProjectsByDateRange(periodStart, periodEnd);
        long newTasks = taskRepository.countNewTasksByDateRange(periodStart, periodEnd);
        
        SystemUsageDashboardResponse.EngagementMetric engagementMetric = SystemUsageDashboardResponse.EngagementMetric.builder()
                .newProjectsCreated(newProjects)
                .newTasksCreated(newTasks)
                .build();

        return SystemUsageDashboardResponse.builder()
                .totalActiveUsers(totalUsers)
                .storageUsage(storageMetric)
                .engagementStats(engagementMetric)
                .build();
    }

    // --- PRIVATE HELPERS ---

    private SystemUsageDashboardResponse.StorageMetric calculateStorageMetrics(long usedBytes) {
        double usagePercentage = 0.0;

        if (SERVER_TOTAL_CAPACITY_BYTES > 0) {
            usagePercentage = ((double) usedBytes / SERVER_TOTAL_CAPACITY_BYTES) * PERCENT_MULTIPLIER;
            usagePercentage = BigDecimal.valueOf(usagePercentage)
                                        .setScale(MATH_SCALE_PERCENTAGE, RoundingMode.HALF_UP)
                                        .doubleValue();
        }

        return SystemUsageDashboardResponse.StorageMetric.builder()
                .totalUsedBytes(usedBytes)
                .totalCapacityBytes(SERVER_TOTAL_CAPACITY_BYTES)
                .usagePercentage(usagePercentage)
                .build();
    }
}