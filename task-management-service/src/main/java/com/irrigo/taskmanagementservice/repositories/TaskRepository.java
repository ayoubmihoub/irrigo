package com.irrigo.taskmanagementservice.repositories;

import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<IrrigationTask, Long> {
    // Tu peux ajouter des méthodes de recherche par zone si besoin
}