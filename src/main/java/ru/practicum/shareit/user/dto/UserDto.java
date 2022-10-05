package ru.practicum.shareit.user.dto;

import lombok.*;
import ru.practicum.shareit.utility.marker.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Email;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {
    private Long id;

    private String name;

    @Email(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    private String email;
}
