package com.quanlyduan.project_manager_api.repository;


import com.quanlyduan.project_manager_api.model.Token;
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    
    Optional<Token> findByTokenAndLoaiToken(String token, TokenType loaiToken);
}