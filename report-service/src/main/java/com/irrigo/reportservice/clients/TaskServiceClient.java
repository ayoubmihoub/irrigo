package com.irrigo.reportservice.clients;

import com.irrigo.reportservice.dto.IrrigationTaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "task-management-service")
public interface TaskServiceClient {
    // Appel vers l'endpoint dynamique du Task Management Service
    @GetMapping("/api/tasks/user/{email}/recent")
    List<IrrigationTaskDTO> getRecentTasks(@PathVariable("email") String email);
}