package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.utility.FromSizeRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemService itemService;

    // Создание нового запроса на вещь
    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestShortDto itemRequestShortDto) {
        final User requester = UserMapper.toUser(userService.findUserById(userId));
        final ItemRequest itemRequest = ItemRequestMapper.toItemRequest(requester, itemRequestShortDto);

        return ItemRequestMapper.toItemRequestDto(
                itemRequestRepository.save(itemRequest),
                itemService.findItemsByRequestId(itemRequest.getId())
        );
    }

    // Получение списка своих запросов вместе с данными об ответах на них
    @Override
    public List<ItemRequestDto> findUserRequests(Long userId) {
        userService.findUserById(userId);
        return itemRequestRepository.findItemRequestsByRequester_IdOrderByCreatedDesc(userId).stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestDto(
                        itemRequest,
                        itemService.findItemsByRequestId(itemRequest.getId())))
                .collect(Collectors.toList());
    }

    // Получение списка запросов, созданных другими пользователями
    @Override
    public List<ItemRequestDto> findAllRequests(Integer from, Integer size, Long userId) {
        Pageable pageable = FromSizeRequest.of(from, size);

        return itemRequestRepository.findAll(pageable).stream()
                .filter(itemRequest -> !itemRequest.getRequester().getId().equals(userId))
                .map(itemRequest -> ItemRequestMapper.toItemRequestDto(
                        itemRequest,
                        itemService.findItemsByRequestId(itemRequest.getId())))
                .collect(Collectors.toList());
    }

    // Получение данных об одном конкретном запросе вместе с данными об ответах на него
    @Override
    public ItemRequestDto findRequestById(Long requestId, Long userId) {
        userService.findUserById(userId);

        List<ItemDto> itemDtos = itemService.findItemsByRequestId(requestId);
        ItemRequest itemRequest = getRequestIfExists(requestId);

        return ItemRequestMapper.toItemRequestDto(itemRequest, itemDtos);
    }

    private ItemRequest getRequestIfExists(Long requestId) {
        String exceptionMessage = "Запрос с id " + requestId + " не найден!";

        if (requestId == null) {
            throw new ValidationException(exceptionMessage);
        }
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(exceptionMessage));
    }
}
