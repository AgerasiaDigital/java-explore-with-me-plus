package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {
    @NotBlank
    @Email
    @Size(min = 6)
    @Size(max = 254)
    private String email;

    private Long id;

    @NotBlank
    @Size(min = 2)
    @Size(max = 250)
    private String name;
}
