package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.Item;

import java.util.List;

public interface ItemStorage {
    Item create(Item item);

    Item findById(Long itemId);

    List<Item> findAllUserItems(Long userId);

    List<Item> findByNameOrDescription(String text);

    Item partialUpdate(Item item);
}
