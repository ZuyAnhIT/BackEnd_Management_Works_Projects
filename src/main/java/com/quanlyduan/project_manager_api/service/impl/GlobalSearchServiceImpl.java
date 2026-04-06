package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.GlobalSearchResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.EpicRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.GlobalSearchService;

@Service
@Transactional(readOnly = true)
public class GlobalSearchServiceImpl implements GlobalSearchService {

    // Define limits to prevent heavy memory consumption and ensure fast UI rendering
    public static final int LIMIT_PROJECTS = 5;
    public static final int LIMIT_EPICS = 5;
    public static final int LIMIT_TASKS = 10;
    
    public static final int MIN_KEYWORD_LENGTH = 2;
    
    public static final String ERROR_KEYWORD_TOO_SHORT = "Search keyword must be at least 2 characters long.";
    public static final String ERROR_UNAUTHENTICATED = "Authenticated user information not found.";
    public static final String FALLBACK_STATUS_NAME = "Unknown";
    public static final String FALLBACK_STATUS_COLOR = "#CCCCCC";

    private final ProjectRepository projectRepository;
    private final EpicRepository epicRepository;
    private final TaskRepository taskRepository;
    private final SecurityService securityService;

    // Manual constructor injection
    public GlobalSearchServiceImpl(ProjectRepository projectRepository,
                                   EpicRepository epicRepository,
                                   TaskRepository taskRepository,
                                   SecurityService securityService) {
        this.projectRepository = projectRepository;
        this.epicRepository = epicRepository;
        this.taskRepository = taskRepository;
        this.securityService = securityService;
    }

    // --- MAIN BUSINESS LOGIC ---

    @Override
    public GlobalSearchResponse searchAll(String keyword) {
        // 1. Validate inputs
        if (keyword == null || keyword.trim().length() < MIN_KEYWORD_LENGTH) {
            throw new BadRequestException(ERROR_KEYWORD_TOO_SHORT);
        }
        
        Integer currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException(ERROR_UNAUTHENTICATED);
        }

        String cleanKeyword = keyword.trim();

        // 2. Prepare pagination limits
        Pageable projectPageable = PageRequest.of(0, LIMIT_PROJECTS);
        Pageable epicPageable = PageRequest.of(0, LIMIT_EPICS);
        Pageable taskPageable = PageRequest.of(0, LIMIT_TASKS);

        // 3. Execute parallel-friendly sequential queries
        // Note: Using 'IN' subquery in JPQL ensures the user is a ProjectMember.
        List<Project> projects = projectRepository.searchProjectsGlobal(currentUserId, cleanKeyword, projectPageable);
        List<Epic> epics = epicRepository.searchEpicsGlobal(currentUserId, cleanKeyword, epicPageable);
        List<Task> tasks = taskRepository.searchTasksGlobal(currentUserId, cleanKeyword, taskPageable);

        // 4. Map and return the grouped response
        return GlobalSearchResponse.builder()
                .projects(mapProjects(projects))
                .epics(mapEpics(epics))
                .tasks(mapTasks(tasks))
                .build();
    }

    // --- PRIVATE MAPPING HELPERS ---

    private List<GlobalSearchResponse.ProjectSearchResult> mapProjects(List<Project> projects) {
        return projects.stream()
                .map(p -> GlobalSearchResponse.ProjectSearchResult.builder()
                        .projectId(p.getId())
                        .projectCode(p.getProjectCode())
                        .projectName(p.getName())
                        .workspaceId(p.getWorkspace() != null ? p.getWorkspace().getId() : null) // Ánh xạ workspaceId
                        .workspaceName(p.getWorkspace() != null ? p.getWorkspace().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }

    private List<GlobalSearchResponse.EpicSearchResult> mapEpics(List<Epic> epics) {
        return epics.stream()
                .map(e -> GlobalSearchResponse.EpicSearchResult.builder()
                        .epicId(e.getId())
                        .epicCode(e.getEpicCode())
                        .epicName(e.getName())
                        .projectId(e.getProject() != null ? e.getProject().getId() : null)
                        .projectName(e.getProject() != null ? e.getProject().getName() : null)
                        // Ánh xạ workspaceId từ Project
                        .workspaceId((e.getProject() != null && e.getProject().getWorkspace() != null) 
                                        ? e.getProject().getWorkspace().getId() : null)
                        .build())
                .collect(Collectors.toList());
    }

    private List<GlobalSearchResponse.TaskSearchResult> mapTasks(List<Task> tasks) {
        return tasks.stream()
                .map(t -> GlobalSearchResponse.TaskSearchResult.builder()
                        .taskId(t.getId())
                        .taskCode(t.getTaskCode())
                        .taskTitle(t.getTitle())
                        .projectId(t.getProject() != null ? t.getProject().getId() : null)
                        .projectName(t.getProject() != null ? t.getProject().getName() : null)
                        // Ánh xạ workspaceId từ Project
                        .workspaceId((t.getProject() != null && t.getProject().getWorkspace() != null) 
                                        ? t.getProject().getWorkspace().getId() : null)
                        .statusName(t.getStatus() != null ? t.getStatus().getName() : FALLBACK_STATUS_NAME)
                        .statusColor(t.getStatus() != null ? t.getStatus().getColor() : FALLBACK_STATUS_COLOR)
                        .build())
                .collect(Collectors.toList());
    }
}