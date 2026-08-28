package gr.aueb.cf10.gymapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TrainerInsertDTO(
        @NotNull(message = "User UUID is required")
        UUID userUuid,

        @NotBlank(message = "Specialty is required")
        @Size(min = 2, max = 100, message = "Specialty must be between 2 and 100 characters")
        String specialty
) {
    public static TrainerInsertDTO empty() {
        return new TrainerInsertDTO(null, "");
    }
}
