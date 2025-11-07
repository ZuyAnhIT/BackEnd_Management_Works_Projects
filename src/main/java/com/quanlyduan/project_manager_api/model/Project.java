package com.quanlyduan.project_manager_api.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "project_type_id")
    // private ProjectType projectType; //Scrum,Kanban

    // @Column(name = "name", nullable = false)
    // // private String name;

    @Column(name = "project_code", nullable = false)
    private String projectCode;

    private String description;
    private String goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.NEW;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate completedAt;

    private BigDecimal progress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
