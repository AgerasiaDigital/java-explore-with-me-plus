package ru.practicum.ewm.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class NewCompilationDto {
    private Set<Long> events;

    private boolean pinned = false;

    @NotBlank
    @Size(min = 1)
    @Size(max = 50)
    private String title;
}
