package com.irrigo.taskmanagementservice.repositories;

import com.irrigo.taskmanagementservice.entities.ETaskStatus;
import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<IrrigationTask, Long> {

    @Query("SELECT DISTINCT t.crop FROM IrrigationTask t WHERE t.userEmail = :email")
    List<String> findDistinctCropsByUser(@Param("email") String email);

    List<IrrigationTask> findByUserEmail(String userEmail);

    // Trouver les tâches planifiées dont l'heure de début est passée ou égale à maintenant
    List<IrrigationTask> findByStatusAndStartTimeBefore(ETaskStatus status, LocalDateTime dateTime);

    // Trouver toutes les tâches en cours pour vérifier leur fin
    List<IrrigationTask> findByStatus(ETaskStatus status);
}