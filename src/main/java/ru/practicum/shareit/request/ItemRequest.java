package ru.practicum.shareit.request;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDate;

/**
 * TODO Sprint add-item-requests.
 */

@Builder
@Data
public class ItemRequest {
    private final Long id;
    private final String description;
    private final User requestor;
    private final LocalDate created;
}
