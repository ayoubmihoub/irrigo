package com.irrigo.taskmanagementservice.repositories;

import com.irrigo.taskmanagementservice.entities.ETaskStatus;
import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<IrrigationTask, Long> {

    List<IrrigationTask> findByUserEmail(String email);

    @Query("SELECT DISTINCT t.crop FROM IrrigationTask t")
    List<String> findDistinctCrops();

    @Query("SELECT DISTINCT t.crop FROM IrrigationTask t WHERE t.userEmail = :email")
    List<String> findDistinctCropsByUser(@Param("email") String email);

    @Query("SELECT SUM(t.waterAmount) FROM IrrigationTask t " +
            "WHERE t.location = :location " +
            "AND t.crop = :crop " +
            "AND t.startTime >= :since " +
            "AND t.status = 'terminated'")
    Double sumWaterAmount(@Param("location") String location,
                          @Param("crop") String crop,
                          @Param("since") LocalDateTime since);

    // --- MÉTHODES POUR LES NOTIFICATIONS ---

    // Pour l'alerte "Dans 15 minutes" : tâches dont le startTime est compris entre T+14 et T+16
    List<IrrigationTask> findByStatusAndStartTimeBetween(ETaskStatus status, LocalDateTime start, LocalDateTime end);

    // Pour l'alerte "Tâche commencée" : tâches planifiées dont l'heure de début est passée
    List<IrrigationTask> findByStatusAndStartTimeBefore(ETaskStatus status, LocalDateTime time);

    // Pour l'alerte "Tâche terminée" : toutes les tâches en cours (ongoing)
    List<IrrigationTask> findByStatus(ETaskStatus status);
}