// File: src/main/java/com/quanlyduan/project_manager_api/repository/ProjectMemberRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ProjectMember;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quanlyduan.project_manager_api.model.Project;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
    
    
    boolean existsByProject_IdAndUser_Id(Integer projectId, Integer userId);

    boolean existsByProject_IdAndUser_IdAndRole_RoleCode(
        Integer projectId, Integer userId, String roleCode
    );
    List<ProjectMember> findByUser_Id(Integer userId); // Đã dịch
    
}
