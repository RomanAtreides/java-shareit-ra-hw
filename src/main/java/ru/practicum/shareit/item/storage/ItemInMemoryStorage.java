package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<Item> userItems = new ArrayList<>();

        items.values().forEach(item -> {
                    if (item.getOwner().equals(userId)) {
                        userItems.add(item);
                    }
        });
        return userItems;
    }

    @Override
    public List<Item> findByNameOrDescription(String text) {
        List<Item> foundItems = new ArrayList<>();

        if (text.isBlank()) {
            return foundItems;
        }

        String finalText = text.toLowerCase();

        items.values().forEach(item -> {
            if (item.getAvailable()) {
                if (item.getDescription().toLowerCase().contains(finalText)
                        || item.getName().toLowerCase().contains(finalText)) {
                    foundItems.add(item);
                }
            }
        });
        return foundItems;
    }

    @Override
    public Item partialUpdate(Item item) {
        return item;
    }

    private Long generateId() {
        return id++;
    }
}
