package gr.aueb.cf10.gymapp.dto;

import gr.aueb.cf10.gymapp.model.enums.PlanType;

import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionReadOnlyDTO(
        UUID uuid,
        UUID memberUuid,
        String memberName,
        PlanType planType,
        LocalDate startDate,
        LocalDate endDate,
        boolean isActive
) {}
