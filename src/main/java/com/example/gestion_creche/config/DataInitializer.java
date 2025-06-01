package com.example.gestion_creche.config;

import com.example.gestion_creche.model.User;
import com.example.gestion_creche.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserService userService, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("Starting data initialization...");

            // Create admin user if it doesn't exist
            User existingAdmin = userService.findByEmail("admin@test.com");
            if (existingAdmin == null) {
                User adminUser = new User();
                adminUser.setEmail("admin@test.com");
                adminUser.setPassword("admin123"); // Will be encoded by UserService
                adminUser.setFirstName("Admin");
                adminUser.setLastName("User");
                adminUser.setActive(true);
                User savedAdmin = userService.registerUser(adminUser, "ROLE_ADMIN");
                System.out.println("Admin user created successfully with role: " + savedAdmin.getRole());
                
                // Verify the admin user was saved correctly
                User verifyAdmin = userService.findByEmail("admin@test.com");
                if (verifyAdmin != null) {
                    System.out.println("Admin user verified in database with role: " + verifyAdmin.getRole());
                    System.out.println("Password matches: " + 
                        passwordEncoder.matches("admin123", verifyAdmin.getPassword()));
                }
            } else {
                System.out.println("Admin user already exists with role: " + existingAdmin.getRole());
            }

            // Create test parent user if it doesn't exist
            User existingParent = userService.findByEmail("parent@test.com");
            if (existingParent == null) {
                User parentUser = new User();
                parentUser.setEmail("parent@test.com");
                parentUser.setPassword("parent123"); // Will be encoded by UserService
                parentUser.setFirstName("Parent");
                parentUser.setLastName("Test");
                parentUser.setActive(true);
                User savedParent = userService.registerUser(parentUser, "ROLE_PARENT");
                System.out.println("Parent user created successfully with role: " + savedParent.getRole());
                
                // Verify the parent user was saved correctly
                User verifyParent = userService.findByEmail("parent@test.com");
                if (verifyParent != null) {
                    System.out.println("Parent user verified in database with role: " + verifyParent.getRole());
                    System.out.println("Password matches: " + 
                        passwordEncoder.matches("parent123", verifyParent.getPassword()));
                }
            } else {
                System.out.println("Parent user already exists with role: " + existingParent.getRole());
            }

            System.out.println("Data initialization completed.");
        };
    }
} 