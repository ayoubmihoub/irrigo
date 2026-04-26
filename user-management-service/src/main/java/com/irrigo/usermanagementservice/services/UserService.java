package com.irrigo.usermanagementservice.services;

import com.irrigo.usermanagementservice.entities.ERole;
import com.irrigo.usermanagementservice.entities.Role;
import com.irrigo.usermanagementservice.entities.User;
import com.irrigo.usermanagementservice.repositories.RoleRepository;
import com.irrigo.usermanagementservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
    // Dans UserService.java
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());

        // Si un nouveau mot de passe est fourni, on l'encode
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Utilisateur non trouvé");
        }
        userRepository.deleteById(id);
    }
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Erreur : Utilisateur non trouvé"));
    }
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("Utilisateur non trouvé avec l'id : " + id);
        }
    }
    // Dans com.irrigo.usermanagementservice.services.UserService.java
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    // Dans UserService.java
    public User updateRole(Long userId, String newRoleName) {
        // 1. Trouver l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Erreur : Utilisateur non trouvé"));

        // 2. Convertir le nom (ex: "admin" ou "user") en ERole
        ERole eRole;
        if (newRoleName.equalsIgnoreCase("admin")) {
            eRole = ERole.ROLE_ADMIN;
        } else {
            eRole = ERole.ROLE_USER;
        }

        // 3. Récupérer le rôle en base
        Role role = roleRepository.findByName(eRole)
                .orElseThrow(() -> new RuntimeException("Erreur : Rôle " + eRole + " non trouvé en base."));

        // 4. Mettre à jour et sauvegarder
        user.setRole(role);
        return userRepository.save(user);
    }
}