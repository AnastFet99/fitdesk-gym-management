package gr.aueb.cf10.gymapp.dto;

import gr.aueb.cf10.gymapp.model.enums.Role;

import java.util.UUID;

public record AuthResponse(
        String token,
        String type,
        UUID userUuid,
        String name,
        String email,
        Role role
) {
    public AuthResponse(String token, UUID userUuid, String name, String email, Role role) {
        this(token, "Bearer", userUuid, name, email, role);
    }
}
