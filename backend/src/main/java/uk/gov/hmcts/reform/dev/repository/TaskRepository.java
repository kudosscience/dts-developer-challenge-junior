package uk.gov.hmcts.reform.dev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dev.models.Task;

/**
 * Repository interface for Task entity.
 * Provides CRUD operations for tasks.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
