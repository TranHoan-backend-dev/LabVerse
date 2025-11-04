package com.se1853_jv.config;

import com.se1853_jv.model.Role;
import com.se1853_jv.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Autowired
    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize default roles if they don't exist
        createRoleIfNotExists("PI");
        createRoleIfNotExists("RESEARCHER");
        createRoleIfNotExists("STUDENT");
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role(roleName);
            roleRepository.save(role);
            System.out.println("Created role: " + roleName);
        }
    }
}









