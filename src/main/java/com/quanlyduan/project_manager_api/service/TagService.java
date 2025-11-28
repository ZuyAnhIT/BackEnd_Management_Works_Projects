package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.dto.response.TagResponse;
import java.util.List;
public interface TagService {
    List<TagResponse> getProjectTags(Integer companyId, Integer workspaceId, Integer projectId, TagFilterRequest filter);
    void assignTagToTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId);
    void removeTagFromTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId);
}
