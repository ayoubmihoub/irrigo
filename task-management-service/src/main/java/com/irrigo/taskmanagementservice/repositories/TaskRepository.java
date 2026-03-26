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

    // RÉFÉRENCE POUR LE BILAN HYDRIQUE : Somme de l'eau versée par parcelle et culture
    @Query("SELECT SUM(t.waterAmount) FROM IrrigationTask t " +
            "WHERE t.location = :location " +
            "AND t.crop = :crop " +
            "AND t.startTime >= :since " +
            "AND t.status = 'terminated'")
    Double sumWaterAmount(@Param("location") String location,
                          @Param("crop") String crop,
                          @Param("since") LocalDateTime since);

    List<IrrigationTask> findByStatusAndStartTimeBefore(ETaskStatus status, LocalDateTime time);
    List<IrrigationTask> findByStatus(ETaskStatus status);
}