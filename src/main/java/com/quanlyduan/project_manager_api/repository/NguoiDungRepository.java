package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
    
    Optional<NguoiDung> findByEmail(String email);
    
    Boolean existsByEmail(String email);
}