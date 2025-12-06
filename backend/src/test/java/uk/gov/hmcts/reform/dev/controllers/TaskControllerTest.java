package uk.gov.hmcts.reform.dev.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.dev.dto.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.exception.GlobalExceptionHandler;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.service.TaskService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskController Unit Tests")
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should create task successfully with valid request")
    void shouldCreateTaskSuccessfully() throws Exception {
        // Given
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .description("Test Description")
            .status(TaskStatus.PENDING)
            .dueDate(dueDate)
            .build();

        TaskResponse expectedResponse = TaskResponse.builder()
            .id(1L)
            .title("Test Task")
            .description("Test Description")
            .status(TaskStatus.PENDING)
            .dueDate(dueDate)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test Task"))
            .andExpect(jsonPath("$.description").value("Test Description"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should create task successfully without optional description")
    void shouldCreateTaskWithoutDescription() throws Exception {
        // Given
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Task Without Description")
            .status(TaskStatus.IN_PROGRESS)
            .dueDate(dueDate)
            .build();

        TaskResponse expectedResponse = TaskResponse.builder()
            .id(2L)
            .title("Task Without Description")
            .description(null)
            .status(TaskStatus.IN_PROGRESS)
            .dueDate(dueDate)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.title").value("Task Without Description"))
            .andExpect(jsonPath("$.description").isEmpty());
    }

    @Test
    @DisplayName("Should return 400 when title is missing")
    void shouldReturn400WhenTitleIsMissing() throws Exception {
        // Given
        CreateTaskRequest request = CreateTaskRequest.builder()
            .description("Test Description")
            .status(TaskStatus.PENDING)
            .dueDate(LocalDateTime.now().plusDays(7))
            .build();

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Should return 400 when title is blank")
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        // Given
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("")
            .status(TaskStatus.PENDING)
            .dueDate(LocalDateTime.now().plusDays(7))
            .build();

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Should return 400 when status is missing")
    void shouldReturn400WhenStatusIsMissing() throws Exception {
        // Given
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .dueDate(LocalDateTime.now().plusDays(7))
            .build();

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Should return 400 when due date is missing")
    void shouldReturn400WhenDueDateIsMissing() throws Exception {
        // Given
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .status(TaskStatus.PENDING)
            .build();

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Should return 400 when due date is in the past")
    void shouldReturn400WhenDueDateIsInPast() throws Exception {
        // Given
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Test Task")
            .status(TaskStatus.PENDING)
            .dueDate(LocalDateTime.now().minusDays(1))
            .build();

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }
}
