package gr.aueb.cf10.gymapp.dto;

import gr.aueb.cf10.gymapp.model.enums.Role;

import java.util.UUID;

public record UserReadOnlyDTO(
        UUID uuid,
        String name,
        String email,
        Role role
) {}
