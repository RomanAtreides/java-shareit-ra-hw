package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createRequest(Long userId, ItemRequestShortDto itemRequestShortDto);

    List<ItemRequestDto> findUserRequests(Long userId);

    List<ItemRequestDto> findAllRequests(Integer from, Integer size, Long userId);

    ItemRequestDto findRequestById(Long requestId, Long userId);
}
