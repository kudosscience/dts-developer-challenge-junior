package uk.gov.hmcts.reform.dev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.dto.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.exception.BankHolidayException;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BankHolidayService bankHolidayService;

    private TaskService taskService;

    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, bankHolidayService);
        futureDate = LocalDateTime.now().plusDays(7);
    }

    @Test
    @DisplayName("Should create task with all fields")
    void shouldCreateTaskWithAllFields() {
        // Given
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .description("Test Description")
            .status(TaskStatus.PENDING)
            .dueDate(futureDate)
            .build();

        Task savedTask = Task.builder()
            .id(1L)
            .title("Test Task")
            .description("Test Description")
            .status(TaskStatus.PENDING)
            .dueDate(futureDate)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        doNothing().when(bankHolidayService).validateNotBankHoliday(futureDate);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // When
        TaskResponse response = taskService.createTask(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertEquals(TaskStatus.PENDING, response.getStatus());
        assertEquals(futureDate, response.getDueDate());
        verify(bankHolidayService).validateNotBankHoliday(futureDate);
    }

    @Test
    @DisplayName("Should create task without description")
    void shouldCreateTaskWithoutDescription() {
        // Given
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Task Without Description")
            .status(TaskStatus.IN_PROGRESS)
            .dueDate(futureDate)
            .build();

        Task savedTask = Task.builder()
            .id(2L)
            .title("Task Without Description")
            .description(null)
            .status(TaskStatus.IN_PROGRESS)
            .dueDate(futureDate)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        doNothing().when(bankHolidayService).validateNotBankHoliday(futureDate);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // When
        TaskResponse response = taskService.createTask(request);

        // Then
        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Task Without Description", response.getTitle());
        assertNull(response.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    @DisplayName("Should pass correct values to repository")
    void shouldPassCorrectValuesToRepository() {
        // Given
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Repository Test")
            .description("Testing repository call")
            .status(TaskStatus.COMPLETED)
            .dueDate(futureDate)
            .build();

        Task savedTask = Task.builder()
            .id(3L)
            .title("Repository Test")
            .description("Testing repository call")
            .status(TaskStatus.COMPLETED)
            .dueDate(futureDate)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        doNothing().when(bankHolidayService).validateNotBankHoliday(futureDate);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // When
        taskService.createTask(request);

        // Then
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task capturedTask = taskCaptor.getValue();
        assertEquals("Repository Test", capturedTask.getTitle());
        assertEquals("Testing repository call", capturedTask.getDescription());
        assertEquals(TaskStatus.COMPLETED, capturedTask.getStatus());
        assertEquals(futureDate, capturedTask.getDueDate());
    }

    @Test
    @DisplayName("Should handle all task statuses")
    void shouldHandleAllTaskStatuses() {
        // Test all status values
        for (TaskStatus status : TaskStatus.values()) {
            // Given
            CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Task with status " + status)
                .status(status)
                .dueDate(futureDate)
                .build();

            Task savedTask = Task.builder()
                .id(1L)
                .title("Task with status " + status)
                .status(status)
                .dueDate(futureDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            doNothing().when(bankHolidayService).validateNotBankHoliday(futureDate);
            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            TaskResponse response = taskService.createTask(request);

            // Then
            assertEquals(status, response.getStatus());
        }
    }

    @Test
    @DisplayName("Should throw BankHolidayException when due date is on a bank holiday")
    void shouldThrowBankHolidayExceptionWhenDueDateIsOnBankHoliday() {
        // Given
        LocalDateTime bankHolidayDate = LocalDateTime.of(2026, 4, 6, 10, 0); // Easter Monday 2026
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .description("Test Description")
            .status(TaskStatus.PENDING)
            .dueDate(bankHolidayDate)
            .build();

        doThrow(new BankHolidayException("Easter Monday", "2026-04-06"))
            .when(bankHolidayService).validateNotBankHoliday(bankHolidayDate);

        // When & Then
        BankHolidayException exception = assertThrows(BankHolidayException.class, () -> {
            taskService.createTask(request);
        });

        assertEquals("Easter Monday", exception.getHolidayName());
        assertEquals("2026-04-06", exception.getHolidayDate());
        verify(taskRepository, never()).save(any(Task.class));
    }
}
