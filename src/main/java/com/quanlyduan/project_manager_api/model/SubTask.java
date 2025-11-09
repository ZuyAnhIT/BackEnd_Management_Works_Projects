// File: src/main/java/com/quanlyduan/project_manager_api/model/SubTask.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sub_tasks") // Khớp bảng 'sub_tasks' (mục 20)
public class SubTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    // SỬA: CSDL dùng 'status ENUM' chứ không dùng 'boolean completed'
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubTaskStatus status = SubTaskStatus.TO_DO;

    // SỬA: Cột trong CSDL là 'parent_task_id'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id", nullable = false)
    private Task task;

    // BỔ SUNG: Cột 'created_by_id' NOT NULL bị thiếu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy; // CSDL yêu cầu 'created_by_id' NOT NULL
}