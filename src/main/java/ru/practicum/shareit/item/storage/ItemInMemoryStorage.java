package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemInMemoryStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private Long id = 1L;

    @Override
    public Item create(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item findById(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> findAllUserItems(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findByNameOrDescription(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        String finalText = text.toLowerCase();

        return items.values().stream()
                .filter(item -> checkItemContainsText(item, finalText))
                .collect(Collectors.toList());
    }

    @Override
    public Item partialUpdate(Item item) {
        return item;
    }

    private Long generateId() {
        return id++;
    }

    private boolean checkItemContainsText(Item item, String text) {
        if (item.getAvailable()) {
            return item.getDescription().toLowerCase().contains(text) || item.getName().toLowerCase().contains(text);
        }
        return false;
    }
}
