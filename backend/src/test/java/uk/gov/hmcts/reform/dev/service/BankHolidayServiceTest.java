package uk.gov.hmcts.reform.dev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.dev.dto.BankHolidayResponse;
import uk.gov.hmcts.reform.dev.exception.BankHolidayException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankHolidayService Unit Tests")
class BankHolidayServiceTest {

    private static final String BANK_HOLIDAYS_URL = "https://www.gov.uk/bank-holidays.json";

    @Mock
    private RestTemplate restTemplate;

    private BankHolidayService bankHolidayService;

    @BeforeEach
    void setUp() {
        bankHolidayService = new BankHolidayService(restTemplate);
    }

    @Test
    @DisplayName("Should identify date as bank holiday")
    void shouldIdentifyDateAsBankHoliday() {
        // Given
        BankHolidayResponse response = createMockBankHolidayResponse();
        when(restTemplate.getForObject(BANK_HOLIDAYS_URL, BankHolidayResponse.class))
            .thenReturn(response);

        // Easter Monday 2026
        LocalDateTime bankHolidayDate = LocalDateTime.of(2026, 4, 6, 10, 0);

        // When
        Optional<BankHolidayResponse.Event> result = bankHolidayService.isBankHoliday(bankHolidayDate);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Easter Monday", result.get().getTitle());
        assertEquals("2026-04-06", result.get().getDate());
    }

    @Test
    @DisplayName("Should return empty when date is not a bank holiday")
    void shouldReturnEmptyWhenDateIsNotBankHoliday() {
        // Given
        BankHolidayResponse response = createMockBankHolidayResponse();
        when(restTemplate.getForObject(BANK_HOLIDAYS_URL, BankHolidayResponse.class))
            .thenReturn(response);

        // Regular working day
        LocalDateTime normalDate = LocalDateTime.of(2026, 4, 7, 10, 0);

        // When
        Optional<BankHolidayResponse.Event> result = bankHolidayService.isBankHoliday(normalDate);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should throw BankHolidayException when validating bank holiday date")
    void shouldThrowBankHolidayExceptionWhenValidatingBankHolidayDate() {
        // Given
        BankHolidayResponse response = createMockBankHolidayResponse();
        when(restTemplate.getForObject(BANK_HOLIDAYS_URL, BankHolidayResponse.class))
            .thenReturn(response);

        // Christmas Day 2026
        LocalDateTime christmasDay = LocalDateTime.of(2026, 12, 25, 9, 0);

        // When & Then
        BankHolidayException exception = assertThrows(BankHolidayException.class, () -> {
            bankHolidayService.validateNotBankHoliday(christmasDay);
        });

        assertTrue(exception.getMessage().contains("Christmas Day"));
        assertEquals("Christmas Day", exception.getHolidayName());
        assertEquals("2026-12-25", exception.getHolidayDate());
    }

    @Test
    @DisplayName("Should not throw exception when validating non-bank-holiday date")
    void shouldNotThrowExceptionWhenValidatingNonBankHolidayDate() {
        // Given
        BankHolidayResponse response = createMockBankHolidayResponse();
        when(restTemplate.getForObject(BANK_HOLIDAYS_URL, BankHolidayResponse.class))
            .thenReturn(response);

        // Regular working day
        LocalDateTime normalDate = LocalDateTime.of(2026, 6, 15, 10, 0);

        // When & Then - no exception should be thrown
        bankHolidayService.validateNotBankHoliday(normalDate);
    }

    @Test
    @DisplayName("Should return empty when API returns null")
    void shouldReturnEmptyWhenApiReturnsNull() {
        // Given
        when(restTemplate.getForObject(BANK_HOLIDAYS_URL, BankHolidayResponse.class))
            .thenReturn(null);

        LocalDateTime anyDate = LocalDateTime.of(2026, 4, 6, 10, 0);

        // When
        Optional<BankHolidayResponse.Event> result = bankHolidayService.isBankHoliday(anyDate);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should handle API exception gracefully")
    void shouldHandleApiExceptionGracefully() {
        // Given
        when(restTemplate.getForObject(BANK_HOLIDAYS_URL, BankHolidayResponse.class))
            .thenThrow(new RestClientException("Connection failed"));

        LocalDateTime anyDate = LocalDateTime.of(2026, 4, 6, 10, 0);

        // When
        Optional<BankHolidayResponse.Event> result = bankHolidayService.isBankHoliday(anyDate);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return all bank holiday dates")
    void shouldReturnAllBankHolidayDates() {
        // Given
        BankHolidayResponse response = createMockBankHolidayResponse();
        when(restTemplate.getForObject(BANK_HOLIDAYS_URL, BankHolidayResponse.class))
            .thenReturn(response);

        // When
        Set<String> dates = bankHolidayService.getAllBankHolidayDates();

        // Then
        assertFalse(dates.isEmpty());
        assertTrue(dates.contains("2026-04-06")); // Easter Monday
        assertTrue(dates.contains("2026-12-25")); // Christmas Day
        assertTrue(dates.contains("2026-01-01")); // New Year's Day
    }

    @Test
    @DisplayName("Should return empty set when API fails")
    void shouldReturnEmptySetWhenApiFails() {
        // Given
        when(restTemplate.getForObject(BANK_HOLIDAYS_URL, BankHolidayResponse.class))
            .thenThrow(new RestClientException("API Error"));

        // When
        Set<String> dates = bankHolidayService.getAllBankHolidayDates();

        // Then
        assertTrue(dates.isEmpty());
    }

    @Test
    @DisplayName("Should handle response with null events list")
    void shouldHandleResponseWithNullEventsList() {
        // Given
        BankHolidayResponse response = new BankHolidayResponse();
        BankHolidayResponse.Division division = new BankHolidayResponse.Division();
        division.setDivision("england-and-wales");
        division.setEvents(null);
        response.setEnglandAndWales(division);

        when(restTemplate.getForObject(BANK_HOLIDAYS_URL, BankHolidayResponse.class))
            .thenReturn(response);

        LocalDateTime anyDate = LocalDateTime.of(2026, 4, 6, 10, 0);

        // When
        Optional<BankHolidayResponse.Event> result = bankHolidayService.isBankHoliday(anyDate);

        // Then
        assertFalse(result.isPresent());
    }

    /**
     * Creates a mock bank holiday response with sample 2026 holidays.
     */
    private BankHolidayResponse createMockBankHolidayResponse() {
        List<BankHolidayResponse.Event> events = Arrays.asList(
            createEvent("New Year's Day", "2026-01-01"),
            createEvent("Good Friday", "2026-04-03"),
            createEvent("Easter Monday", "2026-04-06"),
            createEvent("Early May bank holiday", "2026-05-04"),
            createEvent("Spring bank holiday", "2026-05-25"),
            createEvent("Summer bank holiday", "2026-08-31"),
            createEvent("Christmas Day", "2026-12-25"),
            createEvent("Boxing Day", "2026-12-28")
        );

        BankHolidayResponse.Division englandAndWales = new BankHolidayResponse.Division();
        englandAndWales.setDivision("england-and-wales");
        englandAndWales.setEvents(events);

        BankHolidayResponse response = new BankHolidayResponse();
        response.setEnglandAndWales(englandAndWales);

        return response;
    }

    private BankHolidayResponse.Event createEvent(String title, String date) {
        BankHolidayResponse.Event event = new BankHolidayResponse.Event();
        event.setTitle(title);
        event.setDate(date);
        event.setNotes("");
        event.setBunting(true);
        return event;
    }
}
