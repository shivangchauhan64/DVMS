package com.DVMS.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import com.DVMS.entity.Admin;
import com.DVMS.entity.Visitor;
import com.DVMS.repository.AdminRepository; // Import AdminRepository
import com.DVMS.service.VisitorService;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/visitor")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private AdminRepository adminRepository; // Inject AdminRepository

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("visitor", new Visitor());
        return "register"; // Returns the register.html page
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerVisitor(
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String contact,
            @RequestParam String meetWith,
            @RequestParam String reason,
            @RequestParam Admin.Department department) {

        Visitor visitor = new Visitor();
        visitor.setName(name);
        visitor.setAddress(address);
        visitor.setContact(contact);
        visitor.setMeetWith(meetWith);
        visitor.setReason(reason);
        visitor.setDepartment(department);
        visitor.setStatus("Pending");

        // Validate if the meetWith name exists as an admin
        Admin adminToMeet = adminRepository.findByName(meetWith);
        if (adminToMeet == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid 'meetWith' name. Admin not found.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        visitorService.registerVisitor(visitor);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Visitor registered successfully.");
        response.put("visitorId", visitor.getId()); // Send visitorId
        return ResponseEntity.ok(response);
    }
}