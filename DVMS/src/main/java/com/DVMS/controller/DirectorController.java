package com.DVMS.controller;

import com.DVMS.entity.Admin;
import com.DVMS.entity.Visitor;
import com.DVMS.repository.AdminRepository;
import com.DVMS.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/director")
public class DirectorController {

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private AdminRepository adminRepository;

    @GetMapping("/assigned-requests")
    public String getAssignedRequests(@RequestParam(value = "type", required = false, defaultValue = "pending") String type, Model model) {
        String loggedInUsername = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        Admin loggedInDirector = adminRepository.findByUsername(loggedInUsername).orElse(null);

        if (loggedInDirector != null) {
            String loggedInDirectorName = loggedInDirector.getName();
            List<Visitor> visitors = visitorRepository.findAll();
            List<Visitor> filteredVisitors = visitors.stream()
                    .filter(visitor -> visitor.getMeetWith().equals(loggedInDirectorName))
                    .filter(visitor -> {
                        if (type.equalsIgnoreCase("pending")) {
                            return visitor.getStatus().equalsIgnoreCase("pending");
                        } else if (type.equalsIgnoreCase("approved")) {
                            return visitor.getStatus().equalsIgnoreCase("approved");
                        } else if (type.equalsIgnoreCase("rejected")) {
                            return visitor.getStatus().equalsIgnoreCase("rejected");
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            model.addAttribute("visitors", filteredVisitors);
            model.addAttribute("type", type);
        }
        return "assigned-requests";
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<String> approveRequest(@PathVariable Long id) {
        Visitor visitor = visitorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid visitor ID"));
        visitor.setStatus(com.DVMS.entity.Visitor.VisitorStatus.APPROVED.name());
        visitor.setApprovalTime(LocalDateTime.now());
        String loggedInUsername = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        Admin loggedInDirector = adminRepository.findByUsername(loggedInUsername).orElse(null);
        if (loggedInDirector != null) {
            visitor.setHostApprovedBy(loggedInDirector.getName());
        }
        visitorRepository.save(visitor);
        return ResponseEntity.ok("Approved"); // Return a response
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<String> rejectRequest(@PathVariable Long id, @RequestParam("cancellationReason") String cancellationReason) {
        Visitor visitor = visitorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid visitor ID"));
        visitor.setStatus(com.DVMS.entity.Visitor.VisitorStatus.REJECTED.name());
        visitor.setApprovalTime(LocalDateTime.now());
        visitor.setCancellationReason(cancellationReason);
        String loggedInUsername = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        Admin loggedInDirector = adminRepository.findByUsername(loggedInUsername).orElse(null);
        if (loggedInDirector != null) {
            visitor.setRejectedBy(loggedInDirector.getName()); // Use getName() to get the full name
        }
        visitorRepository.save(visitor);
        return ResponseEntity.ok("Rejected"); // Return a response
    }

    @GetMapping("/dashboard")
    public String directorDashboard(Model model) {
        String loggedInUsername = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        Admin loggedInDirector = adminRepository.findByUsername(loggedInUsername).orElse(null);

        if (loggedInDirector != null) {
            String loggedInDirectorName = loggedInDirector.getName();
            List<Visitor> visitors = visitorRepository.findAll();
            List<Visitor> filteredVisitors = visitors.stream()
                    .filter(visitor -> visitor.getMeetWith().equals(loggedInDirectorName))
                    .collect(Collectors.toList());
            model.addAttribute("visitors", filteredVisitors);
        }
        return "director_dashboard";
    }
}