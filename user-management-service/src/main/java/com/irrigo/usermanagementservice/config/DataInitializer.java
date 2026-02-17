package com.irrigo.usermanagementservice.config;

import com.irrigo.usermanagementservice.entities.ERole;
import com.irrigo.usermanagementservice.entities.Role;
import com.irrigo.usermanagementservice.entities.User;
import com.irrigo.usermanagementservice.repositories.RoleRepository;
import com.irrigo.usermanagementservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; // CHANGEMENT ICI
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // UTILISATION DE L'INTERFACE

    @Override
    public void run(String... args) throws Exception {
        // 1. Création des rôles s'ils n'existent pas
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(null, ERole.ROLE_ADMIN)));

        roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(null, ERole.ROLE_USER)));

        // 2. Création de l'admin par défaut
        if (!userRepository.existsByEmail("admin@irrigo.tn")) {
            User admin = new User();
            admin.setName("Admin Irrigo");
            admin.setEmail("admin@irrigo.tn");
            // On encode le mot de passe avant de l'enregistrer en base
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);
            userRepository.save(admin);
            System.out.println(">>> Compte Admin créé par défaut : admin@irrigo.tn / admin123");
        }
    }
}