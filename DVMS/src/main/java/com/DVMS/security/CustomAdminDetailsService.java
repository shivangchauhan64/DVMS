package com.DVMS.security;

import com.DVMS.entity.Admin;
import com.DVMS.repository.AdminRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CustomAdminDetailsService implements UserDetailsService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomAdminDetailsService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Admin> admin = adminRepository.findByUsername(username);
        if (admin.isEmpty()) {
            throw new UsernameNotFoundException("Admin not found");
        }
        Admin foundAdmin = admin.get();

        return User.withUsername(foundAdmin.getUsername())
                   .password(foundAdmin.getPassword())
                   .roles(foundAdmin.getRole().name()) // Dynamically assign roles
                   .build();
    }
}