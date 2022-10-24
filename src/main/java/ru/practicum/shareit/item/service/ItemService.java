package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, Long userId);

    CommentDto createComment(CommentShortDto commentShortDto, Long itemId, Long userId);

    ItemInfoDto findItemById(Long itemId, Long userId);

    List<ItemInfoDto> findAllUserItems(Long userId, Integer from, Integer size);

    List<ItemDto> findItemsByNameOrDescription(String text, Integer from, Integer size);

    ItemDto updateItem(ItemDto itemDto, Long userId);

    User findOwnerByItemId(Long itemId);

    List<ItemDto> findItemsByRequestId(Long requestId);

    Integer findCountOfUserItems(Long userId);
}
