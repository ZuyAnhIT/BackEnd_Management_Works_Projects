package com.quanlyduan.project_manager_api.util;

import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.SubTask;
import com.quanlyduan.project_manager_api.model.Tag;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.SubTaskRepository;
import com.quanlyduan.project_manager_api.repository.TagRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;

import org.springframework.stereotype.Component;

@Component

public class ProjectHierarchyValidator {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;
    private final SubTaskRepository subTaskRepository;
    
    public ProjectHierarchyValidator(ProjectRepository projectRepository,
                                     TaskRepository taskRepository,
                                     TagRepository tagRepository,
                                     SubTaskRepository subTaskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.tagRepository = tagRepository;
        this.subTaskRepository = subTaskRepository;
    }
    /**
     * Validate Level 1: Project -> Workspace -> Company
     */
    public Project validateProject(Integer companyId, Integer workspaceId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        if (!project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Dự án ID " + projectId + " không thuộc về Workspace ID " + workspaceId);
        }
        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Workspace ID " + workspaceId + " không thuộc về Công ty ID " + companyId);
        }
        return project;
    }

    /**
     * Validate Level 2: Task -> Project -> Workspace -> Company
     */
    public Task validateTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId) {
        // 1. Validate cha trước
        validateProject(companyId, workspaceId, projectId);

        // 2. Validate con
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + taskId));

        if (!task.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Task ID " + taskId + " không thuộc về Project ID " + projectId);
        }
        return task;
    }

    /**
     * Validate Tag: Tag -> Project -> Workspace -> Company (ĐÃ CẬP NHẬT)
     */
    public Tag validateTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId) {
        // 1. Validate cha trước (Đảm bảo Project thuộc Workspace/Company đúng)
        validateProject(companyId, workspaceId, projectId);

        // 2. Validate Tag
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thẻ với ID: " + tagId));
        
        // 3. Kiểm tra Tag có thuộc Project không
        if (!tag.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Thẻ ID " + tagId + " không thuộc về Project ID " + projectId);
        }
        return tag;
    }

    /**
     * Validate SubTask: SubTask -> Task -> Project -> Workspace -> Company
     */
    public SubTask validateSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId) {
        // 1. Validate cha trước (bao gồm cả Project/Workspace/Company)
        validateTask(companyId, workspaceId, projectId, taskId);

        // 2. Validate SubTask
        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc con với ID: " + subTaskId));

        if (!subTask.getParentTask().getId().equals(taskId)) {
            throw new BadRequestException("SubTask ID " + subTaskId + " không thuộc về Task ID " + taskId);
        }
        return subTask;
    }

}