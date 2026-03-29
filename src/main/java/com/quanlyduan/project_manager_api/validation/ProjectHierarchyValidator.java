package com.quanlyduan.project_manager_api.validation;

import org.springframework.stereotype.Component;

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

/**
 * Component ho tro kiem tra tinh hop le cua he thong phan cap du lieu (Hierarchy Validation).
 * Dam bao cac thuc the (Task, Tag, SubTask) thuoc dung cha cua chung (Project, Workspace, Company).
 */
@Component
public class ProjectHierarchyValidator {

    // Khai bao cac hang so thong bao loi
    private static final String ERR_PROJECT_NOT_FOUND = "Project not found with ID: %d";
    private static final String ERR_PROJECT_NOT_IN_WORKSPACE = "Project ID %d does not belong to Workspace ID %d";
    private static final String ERR_WORKSPACE_NOT_IN_COMPANY = "Workspace ID %d does not belong to Company ID %d";
    
    private static final String ERR_TASK_NOT_FOUND = "Task not found with ID: %d";
    private static final String ERR_TASK_NOT_IN_PROJECT = "Task ID %d does not belong to Project ID %d";
    
    private static final String ERR_TAG_NOT_FOUND = "Tag not found with ID: %d";
    private static final String ERR_TAG_NOT_IN_PROJECT = "Tag ID %d does not belong to Project ID %d";
    
    private static final String ERR_SUBTASK_NOT_FOUND = "Subtask not found with ID: %d";
    private static final String ERR_SUBTASK_NOT_IN_TASK = "SubTask ID %d does not belong to Task ID %d";
    
    private static final String ERR_USER_NOT_MEMBER = "User ID %d is not a member of Project ID %d";

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;
    private final SubTaskRepository subTaskRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // Khoi tao thu cong de tiem phu thuoc (Dependency Injection)
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

    /**
     * Validate Cap do 1: Project -> Workspace -> Company.
     * Kiem tra su ton tai va quan he phan cap giua Du an va Cong ty.
     */
    public Project validateProject(Integer companyId, Integer workspaceId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ERR_PROJECT_NOT_FOUND, projectId)));

        // Kiem tra Project thuoc Workspace
        if (!project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException(String.format(ERR_PROJECT_NOT_IN_WORKSPACE, projectId, workspaceId));
        }
        
        // Kiem tra Workspace thuoc Company
        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException(String.format(ERR_WORKSPACE_NOT_IN_COMPANY, workspaceId, companyId));
        }
        
        return project;
    }

    /**
     * Validate Cap do 2: Task -> Project -> Workspace -> Company.
     * Su dung validateProject() de kiem tra cac cap cha truoc khi validate Task.
     */
    public Task validateTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId) {
        validateProject(companyId, workspaceId, projectId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ERR_TASK_NOT_FOUND, taskId)));

        if (!task.getProject().getId().equals(projectId)) {
            throw new BadRequestException(String.format(ERR_TASK_NOT_IN_PROJECT, taskId, projectId));
        }
        
        return task;
    }

    /**
     * Validate Cap do 2: Tag -> Project -> Workspace -> Company.
     * Dam bao nhan (Tag) ton tai va thuoc dung Du an chi dinh.
     */
    public Tag validateTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId) {
        validateProject(companyId, workspaceId, projectId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ERR_TAG_NOT_FOUND, tagId)));

        if (!tag.getProject().getId().equals(projectId)) {
            throw new BadRequestException(String.format(ERR_TAG_NOT_IN_PROJECT, tagId, projectId));
        }
        
        return tag;
    }

    /**
     * Validate Cap do 3: SubTask -> Task -> Project -> Workspace -> Company.
     * Cap do sau nhat, kiem tra toan bo luong phan cap tu SubTask len den Cong ty.
     */
    public SubTask validateSubTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer subTaskId) {
        validateTask(companyId, workspaceId, projectId, taskId);

        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ERR_SUBTASK_NOT_FOUND, subTaskId)));

        if (!subTask.getParentTask().getId().equals(taskId)) {
            throw new BadRequestException(String.format(ERR_SUBTASK_NOT_IN_TASK, subTaskId, taskId));
        }
        
        return subTask;
    }

    /**
     * Kiem tra mot nguoi dung co phai la thanh vien chinh thuc cua du an hay khong.
     * Thuong dung de validate Assignee truoc khi giao viec.
     */
    public void validateProjectMember(Integer projectId, Integer userId) {
        if (userId == null) {
            return;
        }

        boolean isMember = projectMemberRepository.existsByProject_IdAndUser_Id(projectId, userId);
        if (!isMember) {
            throw new BadRequestException(String.format(ERR_USER_NOT_MEMBER, userId, projectId));
        }
    }
}