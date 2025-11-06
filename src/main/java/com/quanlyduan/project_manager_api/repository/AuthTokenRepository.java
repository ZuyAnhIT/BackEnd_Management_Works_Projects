// File: src/main/java/com/quanlyduan/project_manager_api/repository/AuthTokenRepository.java
package com.quanlyduan.project_manager_api.repository;


import com.quanlyduan.project_manager_api.model.AuthToken; // Đã dịch
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Integer> { // Đã dịch
    
    Optional<AuthToken> findByTokenAndTokenType(String token, TokenType tokenType); // Đã dịch
}