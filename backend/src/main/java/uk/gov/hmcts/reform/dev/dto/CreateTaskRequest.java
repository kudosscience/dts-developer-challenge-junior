package uk.gov.hmcts.reform.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for creating a new task.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Request body for creating a new task")
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Schema(description = "The title of the task", example = "Review case documents", required = true)
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Optional description of the task", example = "Review all submitted documents for case ABC123")
    private String description;

    @NotNull(message = "Status is required")
    @Schema(description = "The current status of the task", example = "PENDING", required = true)
    private TaskStatus status;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    @Schema(description = "The due date and time for the task", example = "2025-12-31T17:00:00", required = true)
    private LocalDateTime dueDate;
}
