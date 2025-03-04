package com.DVMS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.DVMS.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
