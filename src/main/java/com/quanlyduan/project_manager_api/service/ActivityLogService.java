package com.quanlyduan.project_manager_api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.model.ActivityLog;
import com.quanlyduan.project_manager_api.repository.ActivityLogRepository;

@Service
public class ActivityLogService {

    public static final String ERROR_SAVE_LOG = "Failed to save activity log";

    private final ActivityLogRepository activityLogRepository;

    // Constructor khoi tao thu cong
    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    /**
     * Luu nhat ky hoat dong moi vao co so du lieu.
     * SU DUNG REQUIRES_NEW de tao mot giao dich doc lap hoan toan. 
     * Neu ghi log loi, giao dich cua nghiep vu chinh (Them/Sua/Xoa) van an toan.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(ActivityLog log) {
        try {
            activityLogRepository.save(log);
        } catch (Exception e) {
            throw new RuntimeException(ERROR_SAVE_LOG);
        }
    }
}