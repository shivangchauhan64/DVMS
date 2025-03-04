package com.DVMS.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Visitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String contact;
    private String meetWith;
    private String reason;
    private String status; // Pending, Approved, Rejected
    private String hostApprovedBy;
    private String rejectedBy;
    private LocalDateTime approvalTime;
    @Column(name = "visitor_id")
    private String visitorId;

    @Column(name = "registration_time")
    private LocalDateTime registrationTime;

    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    @Enumerated(EnumType.STRING)
    private Admin.Department department;

    // Getters and Setters
    
    public Admin.Department getDepartment() {
        return department;
    }

    public void setDepartment(Admin.Department department) {
        this.department = department;
    }

    public String getCancellationReason() {
		return cancellationReason;
	}

	public void setCancellationReason(String cancellationReason) {
		this.cancellationReason = cancellationReason;
	}

	public LocalDateTime getRegistrationTime() {
		return registrationTime;
	}

	public void setRegistrationTime(LocalDateTime registrationTime) {
		this.registrationTime = registrationTime;
	}

	public String getVisitorId() {
		return visitorId;
	}

	public void setVisitorId(String visitorId) {
		this.visitorId = visitorId;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getMeetWith() {
        return meetWith;
    }

    public void setMeetWith(String meetWith) {
        this.meetWith = meetWith;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHostApprovedBy() {
        return hostApprovedBy;
    }

    public void setHostApprovedBy(String hostApprovedBy) {
        this.hostApprovedBy = hostApprovedBy;
    }

    
    
    public String getRejectedBy() {
		return rejectedBy;
	}

	public void setRejectedBy(String rejectedBy) {
		this.rejectedBy = rejectedBy;
	}

	public LocalDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }
    
    public enum VisitorStatus {
        PENDING, APPROVED, REJECTED
    }
}
