package com.DVMS.controller;

import com.DVMS.entity.Admin;
import com.DVMS.entity.Visitor;
import com.DVMS.repository.AdminRepository;
import com.DVMS.service.EmailService;
import com.DVMS.service.UserService;
import com.DVMS.service.VisitorService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class AdminController {

	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	private VisitorService visitorService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserService userService;

	public AdminController(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
		this.adminRepository = adminRepository;
		this.passwordEncoder = passwordEncoder;
	}

	// Show Registration Page (for Admin)
	@GetMapping("/register")
	public String showRegister() {
		return "register"; // Returns the register.html page
	}

	// Show Login Page for Admin
	@GetMapping("/admin/login")
	public String showLoginPage() {
		return "admin-login"; // Returns the login page
	}

	// Admin Dashboard
	@GetMapping("/admin/dashboard")
	public String showDashboard(HttpSession session, Model model) {
		// Get the logged-in admin username from Spring Security context
		String username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
		Optional<Admin> admin = adminRepository.findByUsername(username);

		if (admin.isPresent()) {
			model.addAttribute("admin", admin.get()); // Add admin object to model
			return "admin-dashboard"; // Admin's dashboard view
		}
		return "redirect:/admin/login";
	}

	// Logout Admin
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate(); // Invalidate session
		return "redirect:/admin/login"; // Redirect to login page
	}

	// Show Change Password Form
	@GetMapping("/admin/change-password")
	public String showChangePasswordForm() {
		return "admin-change-password"; // Form to change password
	}

	// Handle Password Change
	@PostMapping("/admin/change-password")
	public String changePassword(@RequestParam("currentPassword") String currentPassword,
			@RequestParam("newPassword") String newPassword, HttpSession session, Model model) {

		// Get the logged-in admin username from Spring Security context
		String username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

		if (username == null) {
			return "redirect:/admin/login";
		}

		Optional<Admin> optionalAdmin = adminRepository.findByUsername(username);

		if (optionalAdmin.isPresent()) {
			Admin admin = optionalAdmin.get();

			// Verify current password
			if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
				model.addAttribute("error", "Current password is incorrect");
				return "admin-change-password"; // Return to password change page
			}

			// Hash the new password and update it
			String encodedNewPassword = passwordEncoder.encode(newPassword);
			admin.setPassword(encodedNewPassword);
			adminRepository.save(admin);

			// Success message after password update
			model.addAttribute("success", "Password changed successfully");
			return "redirect:/admin/dashboard"; // Redirect to dashboard
		} else {
			model.addAttribute("error", "Admin not found");
			return "admin-change-password"; // If admin not found, show error
		}
	}

	// ===================== ADD ADMIN FEATURE =====================

	// Show Admin List with "Add Admin" Button
	@GetMapping("/admin/add-admin")
	public String showAdminList(Model model) {
		List<Admin> admins = adminRepository.findAll();
		model.addAttribute("admins", admins);
		return "admin-add-admin"; // Returns admin-add-admin.html
	}

	@GetMapping("/admins/by-department")
	public ResponseEntity<List<String>> getAdminsByDepartment(@RequestParam("department") Admin.Department department) {
	    List<Admin> admins = adminRepository.findByDepartment(department);
	    List<String> names = admins.stream()
	            .filter(admin -> !admin.getRole().equals(Admin.Role.GUARD))
	            .map(Admin::getName)
	            .collect(Collectors.toList());
	    return ResponseEntity.ok(names);
	}
	
	@GetMapping("/admin/approved-requests")
	public String showApprovedRequests(Model model) {
		List<Visitor> approvedVisitors = visitorService.getVisitorsByStatus("Approved");
		model.addAttribute("visitors", approvedVisitors);
		return "approved-requests"; // Name of your Thymeleaf template
	}

	@GetMapping("/admin/cancelled-requests")
	public String cancelledRequests(Model model, @RequestParam(name = "type", required = false) String type) {
		List<Visitor> visitors;
		if ("rejected".equals(type)) {
			visitors = visitorService.getVisitorsByStatusAndCancellationReason("Rejected", "Admin");
		} else if ("cancelled".equals(type)) {
			visitors = visitorService.getVisitorsByStatusAndCancellationReason("Rejected", "System");
		} else {
			visitors = visitorService.getVisitorsByStatus("Rejected");
		}
		model.addAttribute("visitors", visitors);
		model.addAttribute("type", type);
		return "admin-cancel-request";
	}

	// Show Add Admin Form with roles dropdown
	@GetMapping("/admin/add-admin/form")
	public String showAddAdminForm(Model model) {
		model.addAttribute("admin", new Admin());
		model.addAttribute("roles", Admin.Role.values()); // Pass roles for dropdown
		
		return "admin-add-form"; // Returns add admin form page
	}

	// Handle New Admin Registration with role
	@PostMapping("/admin/add-admin")
	public String addAdmin(@ModelAttribute Admin admin, Model model) {
		// Encode the password before saving
		admin.setPassword(passwordEncoder.encode(admin.getPassword()));
		adminRepository.save(admin);

		model.addAttribute("success", "New Admin Added Successfully!");
		return "redirect:/admin/add-admin"; // Redirect to admin list page
	}

	// Delete Admin
	@GetMapping("/admin/delete/{id}")
	public String deleteAdmin(@PathVariable Long id, Model model) {
		adminRepository.deleteById(id);
		model.addAttribute("success", "Admin deleted successfully!");
		return "redirect:/admin/add-admin"; // Redirect back to admin list page
	}

	@GetMapping("/admin/visitor-log")
	public String showVisitorLog(Model model) {
		model.addAttribute("visitors", visitorService.getVisitorsByStatus("Pending"));
		return "visitorlog"; // This should match your Thymeleaf template name
	}

	@PutMapping("/approve-visitor/{id}")
	public ResponseEntity<String> approveVisitor(@PathVariable Long id) {
		visitorService.approveVisitor(id);
		return ResponseEntity.ok("Visitor approved successfully.");
	}

	@PutMapping("/reject-visitor/{id}")
	public ResponseEntity<String> rejectVisitor(@PathVariable Long id) {
		visitorService.rejectVisitor(id);
		return ResponseEntity.ok("Visitor rejected successfully.");
	}

	@GetMapping("/admin/requests")
	public ResponseEntity<List<Visitor>> viewPendingRequests() {
		List<Visitor> pendingVisitors = visitorService.getVisitorsByStatus("Pending");
		return ResponseEntity.ok(pendingVisitors);
	}

	@PostMapping("/admin/approve/{id}")
	public String approveVisitor(@PathVariable Long id, Model model) {
	    Visitor visitor = visitorService.approveVisitor(id);
	    model.addAttribute("success", "Visitor approved successfully.");
	    return "redirect:/admin/visitor-log";
	}

	@GetMapping("/admin/dashboard-stats")
	public ResponseEntity<Map<String, Integer>> getDashboardStats() {
		Map<String, Integer> stats = new HashMap<>();
		stats.put("totalVisitors", visitorService.getTotalVisitors());
		stats.put("pendingVisitors", visitorService.getPendingVisitors());
		stats.put("approvedRequests", visitorService.getApprovedVisitors());
		return ResponseEntity.ok(stats);
	}

	@PostMapping("/admin/reject/{id}")
	public String rejectVisitor(@PathVariable Long id, Model model) {
		visitorService.rejectVisitor(id);
		model.addAttribute("success", "Visitor rejected successfully.");
		return "redirect:/admin/visitor-log";
	}

}