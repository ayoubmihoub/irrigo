package com.irrigo.usermanagementservice.services;

import com.irrigo.usermanagementservice.entities.ERole;
import com.irrigo.usermanagementservice.entities.Role;
import com.irrigo.usermanagementservice.entities.User;
import com.irrigo.usermanagementservice.repositories.RoleRepository;
import com.irrigo.usermanagementservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Cette méthode doit porter exactement le nom 'saveUser'
    public User saveUser(User user) {
        // 1. Hachage du mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2. Attribution automatique du rôle ROLE_USER
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Erreur : Rôle ROLE_USER non trouvé en base."));
        user.setRole(userRole);

        return userRepository.save(user);
    }
}