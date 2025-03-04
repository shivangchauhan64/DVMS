package com.DVMS.repository;

import com.DVMS.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
//    Optional<Admin> findByEmail(String email);  // Uncomment or add this method if needed
    List<Admin> findByDepartment(Admin.Department department);
    Admin findByName(String name);
}
