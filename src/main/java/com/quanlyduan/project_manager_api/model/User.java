// File: src/main/java/com/quanlyduan/project_manager_api/model/User.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.Gender;
import com.quanlyduan.project_manager_api.model.common.enums.UserStatus;
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
@Table(name = "users") // Đã dịch
public class User { // Đã dịch

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Đã dịch

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false) // Đã dịch
    private String password; // Đã dịch

    @Column(name = "full_name", nullable = false) // Đã dịch
    private String fullName; // Đã dịch

    @Column(name = "avatar_url") // Đã dịch
    private String avatarUrl; // Đã dịch

    @Column(name = "phone_number") // Đã dịch
    private String phoneNumber; // Đã dịch

    @Column(name = "date_of_birth") // Đã dịch
    private LocalDate dateOfBirth; // Đã dịch

    @Enumerated(EnumType.STRING)
    @Column(name = "gender") // Đã dịch
    private Gender gender; // Đã dịch

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) // Đã dịch
    private UserStatus status = UserStatus.ACTIVE; // Đã dịch

    @Builder.Default
    @Column(name = "is_email_verified", nullable = false) // Đã dịch
    private Boolean isEmailVerified = false; // Đã dịch

    @CreationTimestamp
    @Column(name = "created_at", updatable = false) // Đã dịch
    private LocalDateTime createdAt; // Đã dịch

    @UpdateTimestamp
    @Column(name = "updated_at") // Đã dịch
    private LocalDateTime updatedAt; // Đã dịch

    @Column(name = "last_login_at") // Đã dịch
    private LocalDateTime lastLoginAt; // Đã dịch

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) // Đã dịch
    private List<AuthToken> tokens; // Đã dịch (Giả sử Token -> AuthToken)
}
