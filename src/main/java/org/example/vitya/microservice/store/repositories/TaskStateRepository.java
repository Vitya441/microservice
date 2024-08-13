package org.example.vitya.microservice.store.repositories;

import org.example.vitya.microservice.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {
}
