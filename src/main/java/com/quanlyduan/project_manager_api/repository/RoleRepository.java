package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Chúng ta sẽ dùng mã Role để tìm, ví dụ: "COMPANY_ADMIN"
    Optional<Role> findFirstByMaRole(String maRole);
}
