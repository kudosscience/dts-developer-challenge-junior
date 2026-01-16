package uk.gov.hmcts.reform.dev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.dev.dto.BankHolidayResponse;
import uk.gov.hmcts.reform.dev.exception.BankHolidayException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Service for fetching and checking bank holidays from GOV.UK API.
 */
@Service
public class BankHolidayService {

    private static final Logger LOG = LoggerFactory.getLogger(BankHolidayService.class);
    private static final String BANK_HOLIDAYS_URL = "https://www.gov.uk/bank-holidays.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RestTemplate restTemplate;

    public BankHolidayService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Constructor for dependency injection (useful for testing).
     *
     * @param restTemplate the RestTemplate to use for API calls
     */
    public BankHolidayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches bank holidays from GOV.UK API.
     *
     * @return BankHolidayResponse containing bank holidays for all UK regions
     */
    @Cacheable(value = "bankHolidays", unless = "#result == null")
    public BankHolidayResponse fetchBankHolidays() {
        try {
            LOG.info("Fetching bank holidays from GOV.UK API");
            return restTemplate.getForObject(BANK_HOLIDAYS_URL, BankHolidayResponse.class);
        } catch (RestClientException e) {
            LOG.error("Failed to fetch bank holidays from GOV.UK API", e);
            return null;
        }
    }

    /**
     * Checks if a given date falls on a bank holiday (England and Wales).
     *
     * @param dateTime the date to check
     * @return Optional containing the bank holiday event if it's a holiday, empty otherwise
     */
    public Optional<BankHolidayResponse.Event> isBankHoliday(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        String dateString = date.format(DATE_FORMATTER);

        BankHolidayResponse response = fetchBankHolidays();
        if (response == null || response.getEnglandAndWales() == null
            || response.getEnglandAndWales().getEvents() == null) {
            LOG.warn("Unable to validate bank holidays - API unavailable or returned null");
            return Optional.empty();
        }

        return response.getEnglandAndWales().getEvents().stream()
            .filter(event -> dateString.equals(event.getDate()))
            .findFirst();
    }

    /**
     * Validates that the given date is not a bank holiday.
     * Throws BankHolidayException if the date falls on a bank holiday.
     *
     * @param dateTime the date to validate
     * @throws BankHolidayException if the date is a bank holiday
     */
    public void validateNotBankHoliday(LocalDateTime dateTime) {
        Optional<BankHolidayResponse.Event> bankHoliday = isBankHoliday(dateTime);
        if (bankHoliday.isPresent()) {
            BankHolidayResponse.Event holiday = bankHoliday.get();
            throw new BankHolidayException(holiday.getTitle(), holiday.getDate());
        }
    }

    /**
     * Gets all bank holiday dates for England and Wales as a Set.
     *
     * @return Set of bank holiday dates in ISO format (yyyy-MM-dd)
     */
    public Set<String> getAllBankHolidayDates() {
        Set<String> holidays = new HashSet<>();
        BankHolidayResponse response = fetchBankHolidays();

        if (response != null && response.getEnglandAndWales() != null
            && response.getEnglandAndWales().getEvents() != null) {
            response.getEnglandAndWales().getEvents()
                .forEach(event -> holidays.add(event.getDate()));
        }

        return holidays;
    }
}
