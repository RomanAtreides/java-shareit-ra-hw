package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
public class ItemRequestMapper {

    public static ItemRequest toItemRequest(User requester, ItemRequestShortDto itemRequestShortDto) {
        return new ItemRequest(
                itemRequestShortDto.getId(),
                itemRequestShortDto.getDescription(),
                requester,
                itemRequestShortDto.getCreated()
        );
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<ItemDto> itemDtoList) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                itemDtoList
        );
    }
}
