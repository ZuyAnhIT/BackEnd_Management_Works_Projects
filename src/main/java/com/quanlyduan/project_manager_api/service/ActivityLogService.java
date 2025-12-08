package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.model.ActivityLog;
import com.quanlyduan.project_manager_api.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveLog(ActivityLog log) {
        activityLogRepository.save(log);
    }
}