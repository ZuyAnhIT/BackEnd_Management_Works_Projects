// File: src/main/java/com/quanlyduan/project_manager_api/repository/UserRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.User; // Đã dịch
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> { // Đã dịch
    
    Optional<User> findByEmail(String email); // Đã dịch
    
    Boolean existsByEmail(String email);
}