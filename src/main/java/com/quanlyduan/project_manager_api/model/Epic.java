// File: src/main/java/com/quanlyduan/project_manager_api/model/Epic.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "epics")
public class Epic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "epic_code", length = 50)
    private String epicCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "color", length = 7)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EpicStatus status;

    @Column(name = "start_date")
    private LocalDate startDate; // CSDL là DATE

    @Column(name = "due_date")
    private LocalDate dueDate; // CSDL là DATE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // CSDL là TIMESTAMP

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // CSDL là TIMESTAMP
    
    // Quan hệ nghịch đảo: Một Epic có nhiều Task
    // Bảng 'tasks' có cột 'epic_id'
    @OneToMany(mappedBy = "epic")
    private List<Task> tasks;
}