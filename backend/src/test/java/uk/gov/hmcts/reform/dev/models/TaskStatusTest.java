package uk.gov.hmcts.reform.dev.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TaskStatus Enum Tests")
class TaskStatusTest {

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        assertEquals("Pending", TaskStatus.PENDING.getDisplayName());
        assertEquals("In Progress", TaskStatus.IN_PROGRESS.getDisplayName());
        assertEquals("Completed", TaskStatus.COMPLETED.getDisplayName());
        assertEquals("Cancelled", TaskStatus.CANCELLED.getDisplayName());
    }

    @Test
    @DisplayName("Should have four status values")
    void shouldHaveFourStatusValues() {
        assertEquals(4, TaskStatus.values().length);
    }
}
