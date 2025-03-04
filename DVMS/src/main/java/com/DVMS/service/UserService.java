package com.DVMS.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.DVMS.entity.User;
import com.DVMS.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public int getTotalAdmins() {
        return (int) userRepository.count();
    }
    // Authenticate user
    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        return null;
    }
}
