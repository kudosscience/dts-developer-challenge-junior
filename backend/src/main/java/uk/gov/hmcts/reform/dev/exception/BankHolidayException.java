package uk.gov.hmcts.reform.dev.exception;

/**
 * Exception thrown when a task is attempted to be created on a bank holiday.
 */
public class BankHolidayException extends RuntimeException {

    private final String holidayName;
    private final String holidayDate;

    public BankHolidayException(String holidayName, String holidayDate) {
        super(String.format("Cannot create task on bank holiday: %s (%s)", holidayName, holidayDate));
        this.holidayName = holidayName;
        this.holidayDate = holidayDate;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public String getHolidayDate() {
        return holidayDate;
    }
}
