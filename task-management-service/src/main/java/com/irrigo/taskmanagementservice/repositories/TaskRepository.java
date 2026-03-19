package com.irrigo.taskmanagementservice.repositories;

import com.irrigo.taskmanagementservice.entities.ETaskStatus;
import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<IrrigationTask, Long> {

    // 1. Pour l'utilisateur standard : voir ses tâches
    List<IrrigationTask> findByUserEmail(String email);

    // 2. Pour l'Admin : voir toutes les cultures uniques de la plateforme
    @Query("SELECT DISTINCT t.crop FROM IrrigationTask t")
    List<String> findDistinctCrops();

    // 3. Pour l'utilisateur standard : voir ses propres cultures uniques
    @Query("SELECT DISTINCT t.crop FROM IrrigationTask t WHERE t.userEmail = :email")
    List<String> findDistinctCropsByUser(@Param("email") String email);

    // 4. Pour la mise à jour automatique des statuts (TaskService @Scheduled)
    List<IrrigationTask> findByStatusAndStartTimeBefore(ETaskStatus status, LocalDateTime time);
    List<IrrigationTask> findByStatus(ETaskStatus status);
}