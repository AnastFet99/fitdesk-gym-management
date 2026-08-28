package gr.aueb.cf10.gymapp.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.UUID;

public record GymClassInsertDTO(
        @NotBlank(message = "Class name is required")
        String name,

        @NotNull(message = "Trainer UUID is required")
        UUID trainerUuid,

        @Positive(message = "Capacity must be greater than zero")
        int capacity,

        @NotNull(message = "Date and time are required")
        @Future(message = "Class must be scheduled in the future")
        LocalDateTime dateTime
) {
    public static GymClassInsertDTO empty() {
        return new GymClassInsertDTO("", null, 0, null);
    }
}
