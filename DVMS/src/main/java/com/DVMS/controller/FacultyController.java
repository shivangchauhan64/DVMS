package com.DVMS.controller;

import com.DVMS.entity.Admin;
import com.DVMS.entity.Student;
import com.DVMS.entity.Visitor;
import com.DVMS.repository.AdminRepository;
import com.DVMS.repository.StudentRepository;
import com.DVMS.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/faculty")
public class FacultyController {

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @GetMapping("/add-student")
    public String addStudentPage(Model model) {
        model.addAttribute("students", studentRepository.findAllByActiveTrue());
        System.out.println("Students: " + studentRepository.findAllByActiveTrue());
        return "add-student";
    }

    
    @PostMapping("/add-student")
    public String addStudent(
            @RequestParam String uniqueId,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String course,
            @RequestParam int year,
            @RequestParam String rollNumber,
            Model model) {
        Student student = new Student();
        student.setUniqueId(uniqueId);
        student.setName(name);
        student.setEmail(email);
        student.setCourse(course);
        student.setYear(year);
        student.setRollNumber(rollNumber);
        studentRepository.save(student);
        model.addAttribute("students", studentRepository.findAll());
        return "add-student";
    }


    @GetMapping("/assigned-requests")
    public String getAssignedRequests(@RequestParam(value = "type", required = false, defaultValue = "pending") String type, Model model) {
        String loggedInUsername = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        Admin loggedInFaculty = adminRepository.findByUsername(loggedInUsername).orElse(null);

        if (loggedInFaculty != null) {
            String loggedInFacultyName = loggedInFaculty.getName();
            List<Visitor> visitors = visitorRepository.findAll();
            List<Visitor> filteredVisitors = visitors.stream()
                    .filter(visitor -> visitor.getMeetWith().equals(loggedInFacultyName))
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
        Admin loggedInFaculty = adminRepository.findByUsername(loggedInUsername).orElse(null);
        if (loggedInFaculty != null) {
            visitor.setHostApprovedBy(loggedInFaculty.getName());
        }
        visitorRepository.save(visitor);
        return ResponseEntity.ok("Approved");
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<String> rejectRequest(@PathVariable Long id, @RequestParam("cancellationReason") String cancellationReason) {
        Visitor visitor = visitorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid visitor ID"));
        visitor.setStatus(com.DVMS.entity.Visitor.VisitorStatus.REJECTED.name());
        visitor.setApprovalTime(LocalDateTime.now());
        visitor.setCancellationReason(cancellationReason);
        String loggedInUsername = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        Admin loggedInFaculty = adminRepository.findByUsername(loggedInUsername).orElse(null);
        if (loggedInFaculty != null) {
            visitor.setRejectedBy(loggedInFaculty.getName()); // Use getName() to get the full name
        }
        visitorRepository.save(visitor);
        return ResponseEntity.ok("Rejected");
    }

    @GetMapping("/dashboard")
    public String facultyDashboard(Model model) {
        String loggedInUsername = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        Admin loggedInFaculty = adminRepository.findByUsername(loggedInUsername).orElse(null);

        if (loggedInFaculty != null) {
            String loggedInFacultyName = loggedInFaculty.getName();
            List<Visitor> visitors = visitorRepository.findAll();
            List<Visitor> filteredVisitors = visitors.stream()
                    .filter(visitor -> visitor.getMeetWith().equals(loggedInFacultyName))
                    .collect(Collectors.toList());
            model.addAttribute("visitors", filteredVisitors);
        }
        return "faculty_dashboard";
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        long totalVisitors = visitorRepository.count();
        long pendingVisitors = visitorRepository.countByStatus("PENDING");
        long approvedRequests = visitorRepository.countByStatus("APPROVED");
        return ResponseEntity.ok(new DashboardStats(totalVisitors, pendingVisitors, approvedRequests));
    }

    static class DashboardStats {
        private long totalVisitors;
        private long pendingVisitors;
        private long approvedRequests;

        public DashboardStats(long totalVisitors, long pendingVisitors, long approvedRequests) {
            this.totalVisitors = totalVisitors;
            this.pendingVisitors = pendingVisitors;
            this.approvedRequests = approvedRequests;
        }

        public long getTotalVisitors() {
            return totalVisitors;
        }

        public long getPendingVisitors() {
            return pendingVisitors;
        }

        public long getApprovedRequests() {
            return approvedRequests;
        }
    }
    @GetMapping("/edit-student/{id}")
    public String editStudentPage(@PathVariable Long id, Model model) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student ID"));
        model.addAttribute("student", student); // Pass the student object to pre-fill the form
        model.addAttribute("students", studentRepository.findAllByActiveTrue()); // Fetch only active students
        return "add-student";
    }

    @GetMapping("/get-student/{id}")
    @ResponseBody
    public Student getStudent(@PathVariable Long id) {
    	System.out.println("getStudent called with id: " + id);
      Student student= studentRepository.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("Invalid student ID"));
      System.out.println("Student data: " + student); // Add this line
      return student;
    }

    @PostMapping("/save-student")
    public String saveStudent(
        @RequestParam(required = false) Long id,
        @RequestParam String uniqueId,
        @RequestParam String name,
        @RequestParam String email,
        @RequestParam String course,
        @RequestParam int year,
        @RequestParam String rollNumber,
        RedirectAttributes redirectAttributes) {

      Student student;
      if (id != null) {
        student = studentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid student ID"));
      } else {
        student = new Student();
      }
      student.setUniqueId(uniqueId);
      student.setName(name);
      student.setEmail(email);
      student.setCourse(course);
      student.setYear(year);
      student.setRollNumber(rollNumber);
      student.setActive(true);
      student.setPassword("rbmi@123");
      studentRepository.save(student);

      redirectAttributes.addFlashAttribute("successMessage", "Student saved successfully!");
      return "redirect:/faculty/add-student";
    }
    @GetMapping("/delete-student/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student ID"));
        student.setActive(false); // Deactivate the student
        studentRepository.save(student);

        redirectAttributes.addFlashAttribute("successMessage", "Student deactivated successfully!");
        return "redirect:/faculty/add-student";
    }
}