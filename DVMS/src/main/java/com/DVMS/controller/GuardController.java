package com.DVMS.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.DVMS.entity.Visitor;
import com.DVMS.service.VisitorService;

@Controller
@RequestMapping("/guard")
public class GuardController {
	
	@Autowired
    private VisitorService visitorService;

    @GetMapping("/dashboard")
    public String guardDashboard() {
        return "guard-dashboard";
    }
    
    @GetMapping("/send-requests")
    public String sendRequests(Model model) {
        List<Visitor> pendingVisitors = visitorService.getVisitorsByStatus("Pending");
        model.addAttribute("visitors", pendingVisitors);
        return "guard-send-requests";
    }

    @GetMapping("/approved-requests")
    public String approvedRequests(Model model) {
        List<Visitor> approvedVisitors = visitorService.getVisitorsByStatus("Approved");
        model.addAttribute("visitors", approvedVisitors);
        return "guard-approved-requests";
    }

    @GetMapping("/cancelled-requests")
    public String cancelledRequests(Model model, @RequestParam(name = "type", required = false) String type) {
        List<Visitor> visitors;
        if ("rejected".equals(type)) {
            visitors = visitorService.getVisitorsByStatusAndCancellationReason("Rejected", "Admin");
        } else if ("cancelled".equals(type)) {
            visitors = visitorService.getVisitorsByStatusAndCancellationReason("Rejected", "System");
        } else {
            visitors = visitorService.getVisitorsByStatus("Rejected"); // Default: show all
        }
        model.addAttribute("visitors", visitors);
        model.addAttribute("type", type); // Send the type to the view
        return "guard-cancelled-requests";
    }
}