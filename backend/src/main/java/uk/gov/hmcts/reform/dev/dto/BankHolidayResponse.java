package uk.gov.hmcts.reform.dev.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object for GOV.UK bank holidays API response.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankHolidayResponse {

    @JsonProperty("england-and-wales")
    private Division englandAndWales;

    @JsonProperty("scotland")
    private Division scotland;

    @JsonProperty("northern-ireland")
    private Division northernIreland;

    /**
     * Represents a division (region) with its bank holiday events.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Division {
        private String division;
        private List<Event> events;
    }

    /**
     * Represents a single bank holiday event.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Event {
        private String title;
        private String date;
        private String notes;
        private boolean bunting;
    }
}
