package gr.aueb.cf10.gymapp.dto;

import gr.aueb.cf10.gymapp.model.enums.PlanType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionInsertDTO(
        @NotNull(message = "Member UUID is required")
        UUID memberUuid,

        @NotNull(message = "Plan type is required")
        PlanType planType,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        @Future(message = "End date must be in the future")
        LocalDate endDate
) {
    public static SubscriptionInsertDTO empty() {
        return new SubscriptionInsertDTO(null, null, null, null);
    }
}
