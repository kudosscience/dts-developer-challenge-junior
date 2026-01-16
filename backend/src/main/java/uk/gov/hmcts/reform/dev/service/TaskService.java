package uk.gov.hmcts.reform.dev.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dev.dto.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

/**
 * Service class for task operations.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final BankHolidayService bankHolidayService;

    public TaskService(TaskRepository taskRepository, BankHolidayService bankHolidayService) {
        this.taskRepository = taskRepository;
        this.bankHolidayService = bankHolidayService;
    }

    /**
     * Creates a new task.
     * Validates that the due date does not fall on a bank holiday.
     *
     * @param request the task creation request
     * @return the created task response
     * @throws uk.gov.hmcts.reform.dev.exception.BankHolidayException if due date is on a bank holiday
     */
    public TaskResponse createTask(CreateTaskRequest request) {
        // Validate that the due date is not a bank holiday
        bankHolidayService.validateNotBankHoliday(request.getDueDate());

        Task task = Task.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .status(request.getStatus())
            .dueDate(request.getDueDate())
            .build();

        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
    }
}
