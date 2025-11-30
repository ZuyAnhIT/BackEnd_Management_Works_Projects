// File: src/main/java/com/quanlyduan/project_manager_api/validation/ProjectHierarchyValidator.java
package com.quanlyduan.project_manager_api.validation;

import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.SubTask;
import com.quanlyduan.project_manager_api.model.Tag;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
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
    private final ProjectMemberRepository projectMemberRepository;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public ProjectHierarchyValidator(ProjectRepository projectRepository,
                                     TaskRepository taskRepository,
                                     TagRepository tagRepository,
                                     SubTaskRepository subTaskRepository,
                                     ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.tagRepository = tagRepository;
        this.subTaskRepository = subTaskRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    // ======================================================
    // 1. VALIDATE PROJECT (LEVEL 1)
    // ======================================================
    /**
     * Validate Level 1: Project -> Workspace -> Company.
     * Đảm bảo Project tồn tại và thuộc đúng Workspace/Company.
     */
    public Project validateProject(Integer companyId, Integer workspaceId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        // Kiểm tra Project thuộc Workspace
        if (!project.getWorkspace().getId().equals(workspaceId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project ID " + projectId + " does not belong to Workspace ID " + workspaceId);
        }
        // Kiểm tra Workspace thuộc Company
        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace ID " + workspaceId + " does not belong to the Company ID " + companyId);
        }
        return project;
    }

    // ======================================================
    // 2. VALIDATE TASK (LEVEL 2)
    // ======================================================
    /**
     * Validate Level 2: Task -> Project -> Workspace -> Company.
     * Sử dụng validateProject() để kiểm tra các cấp cha trước.
     */
    public Task validateTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId) {
        // 1. Validate cha trước (Project, Workspace, Company)
        validateProject(companyId, workspaceId, projectId);

        // 2. Validate Task
        Task task = taskRepository.findById(taskId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID:" + taskId));

        // Kiểm tra Task thuộc Project
        if (!task.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Task ID " + taskId + " does not belong to Project ID " + projectId);
        }
        return task;
    }

    // ======================================================
    // 3. VALIDATE TAG (LEVEL 2)
    // ======================================================
    /**
     * Validate Tag: Tag -> Project -> Workspace -> Company.
     * Đảm bảo Tag tồn tại và thuộc đúng Project.
     */
    public Tag validateTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId) {
        // 1. Validate cha trước (Project, Workspace, Company)
        validateProject(companyId, workspaceId, projectId);

        // 2. Validate Tag
        Tag tag = tagRepository.findById(tagId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with ID:" + tagId));

        // 3. Kiểm tra Tag có thuộc Project không
        if (!tag.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Tag ID " + tagId + " does not belong to Project ID " + projectId);
        }
        return tag;
    }

    // ======================================================
    // 4. VALIDATE SUBTASK (LEVEL 3)
    // ======================================================
    /**
     * Validate SubTask: SubTask -> Task -> Project -> Workspace -> Company.
     * Sử dụng validateTask() để kiểm tra các cấp cha trước.
     */
    public SubTask validateSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId) {
        // 1. Validate cha trước (Task, Project, Workspace, Company)
        validateTask(companyId, workspaceId, projectId, taskId);

        // 2. Validate SubTask
        SubTask subTask = subTaskRepository.findById(subTaskId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found with ID:" + subTaskId));

        // Kiểm tra SubTask thuộc Task
        if (!subTask.getParentTask().getId().equals(taskId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("SubTask ID " + subTaskId + " does not belong to Task ID " + taskId);
        }
        return subTask;
    }

    // ======================================================
    // 5. VALIDATE THÀNH VIÊN DỰ ÁN
    // ======================================================
    /**
     * 2. Validate Assignee: Kiểm tra người được giao có thuộc Project không.
     * Hàm này trả về void, nếu sai thì ném lỗi. Bỏ qua nếu userId là null.
     */
    public void validateProjectMember(Integer projectId, Integer userId) {
        // Nếu userId là null (trường hợp không giao cho ai hoặc gỡ người làm), thì bỏ qua
        if (userId == null) {
            return;
        }

        // Kiểm tra xem userId có tồn tại trong bảng ProjectMember (với projectId đã cho) không
        boolean isMember = projectMemberRepository.existsByProject_IdAndUser_Id(projectId, userId);
        if (!isMember) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("User ID " + userId + " is not a member of Project ID " + projectId);
        }
    }
}