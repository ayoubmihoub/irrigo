package com.irrigo.taskmanagementservice.repositories;

import com.irrigo.taskmanagementservice.entities.IrrigationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TaskRepository extends JpaRepository<IrrigationTask, Long> {

    // Requête pour récupérer les cultures uniques
    @Query("SELECT DISTINCT t.crop FROM IrrigationTask t")
    List<String> findDistinctCrops();
}