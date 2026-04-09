package com.irrigo.taskmanagementservice.repositories;

import com.irrigo.taskmanagementservice.entities.ETaskStatus;
import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<IrrigationTask, Long> {

    // Récupérer toutes les tâches d'un utilisateur
    List<IrrigationTask> findByUserEmail(String email);

    // Récupérer les tâches d'un utilisateur après une date précise (pour le rapport 30 jours)
    List<IrrigationTask> findByUserEmailAndStartTimeAfter(String email, LocalDateTime date);

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
    List<IrrigationTask> findByStatusAndStartTimeBetween(ETaskStatus status, LocalDateTime start, LocalDateTime end);
    List<IrrigationTask> findByStatusAndStartTimeBefore(ETaskStatus status, LocalDateTime time);
    List<IrrigationTask> findByStatus(ETaskStatus status);
}