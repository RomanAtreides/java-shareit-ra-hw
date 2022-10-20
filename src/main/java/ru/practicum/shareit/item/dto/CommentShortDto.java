package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.utility.marker.Create;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentShortDto {
    private Long id;

    @NotBlank(groups = {Create.class})
    private String text;

    private LocalDateTime created = LocalDateTime.now();
}
