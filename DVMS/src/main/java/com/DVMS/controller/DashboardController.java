package com.DVMS.controller;

import com.DVMS.entity.Admin;
import com.DVMS.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private AdminRepository adminRepository;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal User principal, Model model) {
        String username = principal.getUsername();
        Admin admin = adminRepository.findByUsername(username).orElse(null);

        if (admin != null) {
            switch (admin.getRole()) {
                case ADMIN:
                    return "admin-dashboard";
                case DIRECTOR:
                    model.addAttribute("director", admin);
                    return "director-dashboard";
                case FACULTY:
                    model.addAttribute("faculty", admin);
                    return "faculty-dashboard";
                case VP:
                    model.addAttribute("vp", admin);
                    return "vp-dashboard";
                case GUARD:
                    return "guard-dashboard";
                default:
                    return "redirect:/";
            }
        }
        return "redirect:/";
    }
}