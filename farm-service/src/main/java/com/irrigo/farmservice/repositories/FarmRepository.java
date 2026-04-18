package com.irrigo.farmservice.repositories;

import com.irrigo.farmservice.entities.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {

    /**
     * Récupère la liste de tous les champs (farms) appartenant à un utilisateur spécifique.
     * Utilisé dans FarmService pour filtrer les données par email.
     */
    List<Farm> findByUserEmail(String email);

    /**
     * Optionnel : Trouver un champ par son nom pour un utilisateur donné.
     */
    List<Farm> findByNameContainingIgnoreCaseAndUserEmail(String name, String email);
}