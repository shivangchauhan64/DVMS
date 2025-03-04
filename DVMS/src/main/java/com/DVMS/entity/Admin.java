package com.DVMS.entity;

import jakarta.persistence.*;

@Entity
public class Admin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;
	private String password;
	private String name;
	@Enumerated(EnumType.STRING)
    private Department department;
	private String contactNumber;
	

	@Enumerated(EnumType.STRING)
	private Role role;

	// Getters and Setters (Include getters and setters for all new fields)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public enum Role {
		ADMIN, VP, DIRECTOR, FACULTY, GUARD
	}
	
	public enum Department {
        RBCET, RBCP, RBCN, RBMI, RBTTI
    }
}