package com.irrigo.usermanagementservice.controllers;

import com.irrigo.usermanagementservice.dto.LoginRequest;
import com.irrigo.usermanagementservice.entities.User;
import com.irrigo.usermanagementservice.repositories.UserRepository;
import com.irrigo.usermanagementservice.security.JwtUtils;
import com.irrigo.usermanagementservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder encoder;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erreur : Utilisateur non trouvé");
        }

        if (encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String role = user.getRole().getName().toString();
            String jwt = jwtUtils.generateJwtToken(user.getEmail(), role);

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId()); // AJOUT DE L'ID ICI
            response.put("token", jwt);
            response.put("role", role);
            response.put("name", user.getName());
            response.put("email", user.getEmail());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erreur : Mot de passe incorrect");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Erreur : Cet email est déjà utilisé");
        }
        return ResponseEntity.ok(userService.saveUser(user));
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id, @RequestBody User userDetails) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User userToUpdate = userRepository.findById(id).orElse(null);

        if (userToUpdate == null) return ResponseEntity.notFound().build();

        if (!userToUpdate.getEmail().equals(currentUserEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Erreur : Vous ne pouvez modifier que votre propre compte.");
        }

        return ResponseEntity.ok(userService.updateUser(id, userDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User userToDelete = userRepository.findById(id).orElse(null);

        if (userToDelete == null) return ResponseEntity.notFound().build();

        if (!userToDelete.getEmail().equals(currentUserEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Erreur : Vous ne pouvez supprimer que votre propre compte.");
        }

        userService.deleteUserById(id);
        return ResponseEntity.ok("Compte supprimé avec succès.");
    }
    @Autowired
    private com.irrigo.usermanagementservice.clients.TaskServiceClient taskServiceClient;

    // 1. Afficher toutes les tasks (via Feign)
    @GetMapping("/admin/tasks/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllTasksForAdmin() {
        try {
            return ResponseEntity.ok(taskServiceClient.getAllTasksFromService());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la récupération des tâches : " + e.getMessage());
        }
    }

    // 2. Supprimer n'importe quel utilisateur
    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAnyUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
    // 3. Afficher tous les utilisateurs (Réservé à l'Admin)
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsersForAdmin() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/admin/users/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String newRole = request.get("role"); // Le frontend doit envoyer {"role": "admin"} ou {"role": "user"}
            User updatedUser = userService.updateRole(id, newRole);
            return ResponseEntity.ok("Rôle mis à jour avec succès pour " + updatedUser.getName());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}