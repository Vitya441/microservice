package org.example.vitya.microservice.store.repositories;

import org.example.vitya.microservice.store.entities.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
}
