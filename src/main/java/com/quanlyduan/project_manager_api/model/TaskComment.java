// File: src/main/java/com/quanlyduan/project_manager_api/model/TaskComment.java
package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
// import java.util.Set; // SỬA: Xóa import

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_comments") // Khớp bảng 'task_comments' (mục 22)
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // SỬA: Cột trong CSDL là 'commenter_id'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commenter_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // SỬA: XÓA BỎ HOÀN TOÀN QUAN HỆ @ManyToMany
    // Bảng 'task_comment_mentions' không tồn tại trong CSDL.
    /*
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
          name = "task_comment_mentions",
          joinColumns = @JoinColumn(name = "comment_id"),
          inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> mentionedUsers;
    */
}