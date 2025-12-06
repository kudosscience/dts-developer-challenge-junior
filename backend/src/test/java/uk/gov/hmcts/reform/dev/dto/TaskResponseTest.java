package uk.gov.hmcts.reform.dev.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("TaskResponse DTO Tests")
class TaskResponseTest {

    @Test
    @DisplayName("Should convert entity to response correctly")
    void shouldConvertEntityToResponse() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(7);
        
        Task task = Task.builder()
            .id(1L)
            .title("Test Task")
            .description("Test Description")
            .status(TaskStatus.PENDING)
            .dueDate(dueDate)
            .createdAt(now)
            .updatedAt(now)
            .build();

        // When
        TaskResponse response = TaskResponse.fromEntity(task);

        // Then
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertEquals(TaskStatus.PENDING, response.getStatus());
        assertEquals(dueDate, response.getDueDate());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle null description in entity")
    void shouldHandleNullDescription() {
        // Given
        Task task = Task.builder()
            .id(2L)
            .title("Task Without Description")
            .description(null)
            .status(TaskStatus.IN_PROGRESS)
            .dueDate(LocalDateTime.now().plusDays(5))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When
        TaskResponse response = TaskResponse.fromEntity(task);

        // Then
        assertNull(response.getDescription());
    }
}
