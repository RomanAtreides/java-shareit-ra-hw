package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long owner;
    private ItemRequest request;
}
