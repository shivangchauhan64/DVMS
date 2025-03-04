package com.DVMS.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordVerifier {
    public static void main(String[] args) {
    	 BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
         String storedHash = "$2a$10$.iF8mlp0ZIpfYeA8faMBjuyVXAvqic7zjBpgNWKGEzR7U.kQTTcCW"; // Your DB-stored hash
         String userInputPassword = "admin"; // The password you're trying

         boolean matches = encoder.matches(userInputPassword, storedHash);
         System.out.println("Password Match: " + matches);
    }
}

