package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateEpicRequest;
import com.quanlyduan.project_manager_api.dto.response.EpicResponse;
import java.util.List;

public interface EpicService {
    
    
    List<EpicResponse> getEpicsByProject(Integer projectId, String keyword);
    
    EpicResponse createEpic(Integer projectId, CreateEpicRequest request);

    EpicResponse updateEpic(Integer projectId, Integer epicId, UpdateEpicRequest request);
    // ... (Các hàm CRUD khác sẽ được thêm sau)
    
}