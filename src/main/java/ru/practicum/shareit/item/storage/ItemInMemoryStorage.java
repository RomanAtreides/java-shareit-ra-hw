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

    private Long generateId() {
        return id++;
    }

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

        for (Map.Entry<Long, Item> entry : items.entrySet()) {
            if (entry.getValue().getOwner().equals(userId)) {
                userItems.add(entry.getValue());
            }
        }
        return userItems;
    }

    @Override
    public List<Item> findByNameOrDescription(String text) {
        List<Item> foundItems = new ArrayList<>();

        if (text.isBlank()) {
            return foundItems;
        }

        text = text.toLowerCase();

        for (Item item : items.values()) {
            if (item.getAvailable()) {
                if (item.getDescription().toLowerCase().contains(text)
                        || item.getName().toLowerCase().contains(text)) {
                    foundItems.add(item);
                }
            }
        }
        return foundItems;
    }

    @Override
    public Item partialUpdate(Item item) {
        return item;
    }
}
