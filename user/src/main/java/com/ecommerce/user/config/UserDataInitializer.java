package com.ecommerce.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ecommerce.user.models.Role;
import com.ecommerce.user.repository.RoleRepository;

@Component
public class UserDataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setDescription("Default user role");
            roleRepository.save(userRole);

            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Administrator role");
            roleRepository.save(adminRole);

            Role moderatorRole = new Role();
            moderatorRole.setName("ROLE_MODERATOR");
            moderatorRole.setDescription("Moderator role");
            roleRepository.save(moderatorRole);

            System.out.println("✅ Default roles initialized successfully");
        }
    }
}
