package gr.aueb.cf10.gymapp.dto;

import gr.aueb.cf10.gymapp.model.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingReadOnlyDTO(
        UUID uuid,
        UUID memberUuid,
        String memberName,
        UUID gymClassUuid,
        String gymClassName,
        LocalDateTime classDateTime,
        BookingStatus status,
        LocalDateTime createdAt
) {}
