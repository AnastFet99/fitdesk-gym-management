package gr.aueb.cf10.gymapp.dto;

import gr.aueb.cf10.gymapp.model.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BookingInsertDTO(
        @NotNull(message = "Member UUID is required")
        UUID memberUuid,

        @NotNull(message = "Gym class UUID is required")
        UUID gymClassUuid,

        BookingStatus status
) {
    public static BookingInsertDTO empty() {
        return new BookingInsertDTO(null, null, null);
    }
}
