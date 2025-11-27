package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.EpicResponse;
import java.util.List;

public interface EpicService {
    
    
    List<EpicResponse> getEpicsByProject(Integer projectId, String keyword);
    
    // ... (Các hàm CRUD khác sẽ được thêm sau)
    
}