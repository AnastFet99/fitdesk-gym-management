package gr.aueb.cf10.gymapp.dto;

import java.util.UUID;

public record MemberReadOnlyDTO(
        UUID uuid,
        UUID userUuid,
        String userName,
        String userEmail,
        String phone,
        UUID subscriptionUuid
) {}
