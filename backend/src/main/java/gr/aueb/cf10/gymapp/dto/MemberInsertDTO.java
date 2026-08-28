package gr.aueb.cf10.gymapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record MemberInsertDTO(
        @NotNull(message = "User UUID is required")
        UUID userUuid,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone must be a valid number (10-15 digits)")
        String phone
) {
    public static MemberInsertDTO empty() {
        return new MemberInsertDTO(null, "");
    }
}
