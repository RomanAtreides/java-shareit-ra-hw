package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.utility.marker.Create;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentShortDto {

    private Long id;

    @NotBlank(groups = {Create.class})
    private String text;

    private LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
}
