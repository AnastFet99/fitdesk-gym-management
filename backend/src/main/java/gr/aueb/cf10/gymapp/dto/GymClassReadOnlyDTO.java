package gr.aueb.cf10.gymapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GymClassReadOnlyDTO(
        UUID uuid,
        String name,
        UUID trainerUuid,
        String trainerName,
        String trainerSpecialty,
        int capacity,
        LocalDateTime dateTime
) {}
