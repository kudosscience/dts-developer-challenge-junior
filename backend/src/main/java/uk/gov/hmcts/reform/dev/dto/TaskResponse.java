package uk.gov.hmcts.reform.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for task responses.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Response body containing task details")
public class TaskResponse {

    @Schema(description = "Unique identifier of the task", example = "1")
    private Long id;

    @Schema(description = "The title of the task", example = "Review case documents")
    private String title;

    @Schema(description = "Optional description of the task", example = "Review all submitted documents for case ABC123")
    private String description;

    @Schema(description = "The current status of the task", example = "PENDING")
    private TaskStatus status;

    @Schema(description = "The due date and time for the task", example = "2025-12-31T17:00:00")
    private LocalDateTime dueDate;

    @Schema(description = "Timestamp when the task was created", example = "2025-12-06T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the task was last updated", example = "2025-12-06T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * Creates a TaskResponse from a Task entity.
     *
     * @param task the task entity
     * @return the task response DTO
     */
    public static TaskResponse fromEntity(Task task) {
        return TaskResponse.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .dueDate(task.getDueDate())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
}
