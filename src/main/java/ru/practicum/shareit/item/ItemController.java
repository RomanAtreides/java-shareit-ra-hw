package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(
            @RequestBody Item item,
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        return ItemMapper.toItemDto(itemService.create(item, userId));
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Long itemId) {
        return ItemMapper.toItemDto(itemService.findById(itemId));
    }

    @GetMapping
    public List<ItemDto> findAllUserItems(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        return itemService.findAllUserItems(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> findByNameOrDescription(@RequestParam String text) {
        return itemService.findByNameOrDescription(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{itemId}")
    public ItemDto partialUpdate(
            @RequestBody Item item,
            @PathVariable Long itemId,
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        return ItemMapper.toItemDto(itemService.partialUpdate(item, itemId, userId));
    }
}
