package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.utility.Validator;

import java.util.List;

@Service
public class ItemService {
    private final ItemStorage itemStorage;
    private final Validator validator;

    @Autowired
    public ItemService(ItemStorage itemStorage, Validator validator) {
        this.itemStorage = itemStorage;
        this.validator = validator;
    }

    public Item create(Item item, Long userId) {
        validator.validateItem(item);
        validator.checkUserId(userId);
        item.setOwner(userId);
        return itemStorage.create(item);
    }

    public Item findById(Long itemId) {
        validator.checkItemId(itemId);
        return itemStorage.findById(itemId);
    }

    public List<Item> findAllUserItems(Long userId) {
        validator.checkUserId(userId);
        return itemStorage.findAllUserItems(userId);
    }

    public List<Item> findByNameOrDescription(String text) {
        return itemStorage.findByNameOrDescription(text);
    }

    public Item partialUpdate(Item item, Long itemId, Long userId) {
        validator.checkItemId(itemId);
        validator.checkUserId(userId);
        return itemStorage.partialUpdate(update(item, itemId, userId));
    }

    private Item update(Item item, Long itemId, Long userId) {
        Item oldItem = itemStorage.findById(itemId);
        String name = item.getName();
        String description = item.getDescription();
        Boolean isAvailable = item.getAvailable();

        validator.checkOwner(oldItem.getOwner(), userId);

        if (name != null && !name.equals(oldItem.getName())) {
            oldItem.setName(name);
        }

        if (description != null && !description.equals(oldItem.getDescription())) {
            oldItem.setDescription(description);
        }

        if (isAvailable != null && isAvailable != oldItem.getAvailable()) {
            oldItem.setAvailable(isAvailable);
        }
        return oldItem;
    }
}
