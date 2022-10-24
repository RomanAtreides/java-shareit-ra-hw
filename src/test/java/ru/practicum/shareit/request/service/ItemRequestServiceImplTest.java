package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.utility.FromSizeRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ItemRequestServiceImplTest {

    private ItemRequestRepository itemRequestRepository;
    private UserService userService;
    private ItemService itemService;
    private ItemRequestServiceImpl itemRequestService;

    @BeforeEach
    void setUp() {
        itemRequestRepository = mock(ItemRequestRepository.class);
        userService = mock(UserService.class);
        itemService = mock(ItemService.class);

        when(itemRequestRepository.save(any())).then(invocation -> invocation.getArgument(0));

        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, userService, itemService);
    }

    @Test
    void shouldCreateRequestWhenUserAndItemExist() {
        ItemRequestShortDto itemRequestShortDto = makeItemRequestShortDto();
        UserDto userDto = makeUser(1L, "userOne", "userOne@email.com");
        Long userId = userDto.getId();
        List<ItemDto> items = makeItems();

        when(userService.findUserById(any())).thenReturn(userDto);
        when(itemService.findItemsByRequestId(any())).thenReturn(items);

        ItemRequestDto result = itemRequestService.createRequest(userId, itemRequestShortDto);

        assertNotNull(result);
        assertEquals(itemRequestShortDto.getId(), result.getId());
        assertEquals(itemRequestShortDto.getDescription(), result.getDescription());
        assertEquals(itemRequestShortDto.getCreated(), result.getCreated());
        assertEquals(items, result.getItems());
        assertEquals(items.size(), result.getItems().size());
        verify(userService, times(1)).findUserById(userId);
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
        verify(itemService, times(1)).findItemsByRequestId(any(Long.class));
    }

    @Test
    void shouldFindUserRequestsWhenUserExists() {
        UserDto userDto = makeUser(2L, "userTwo", "userTwo@email.com");
        Long userId = userDto.getId();

        when(userService.findUserById(any()))
                .thenReturn(userDto);
        when(itemRequestRepository.findItemRequestsByRequester_IdOrderByCreatedDesc(userId))
                .thenReturn(makeItemRequests().stream()
                        .filter(itemRequest -> itemRequest.getRequester().getId().equals(userId))
                        .collect(Collectors.toList())
                );

        when(itemService.findItemsByRequestId(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Long id = invocationOnMock.getArgument(0);

                    return makeItems().stream()
                            .filter(itemDto -> itemDto.getRequestId().equals(id))
                            .collect(Collectors.toList());
                });

        List<ItemRequestDto> result = itemRequestService.findUserRequests(userId);

        assertEquals(makeItems().get(1), result.get(0).getItems().get(0));
        assertEquals(makeItems().get(3), result.get(0).getItems().get(1));
        assertEquals(makeItems().get(4), result.get(1).getItems().get(0));
        verify(userService, times(1)).findUserById(userId);
        verify(itemService, atLeastOnce()).findItemsByRequestId(any());
        verify(itemRequestRepository, times(1))
                .findItemRequestsByRequester_IdOrderByCreatedDesc(userId);
    }

    @Test
    void shouldFindAllRequests() {
        int from = 0;
        int size = 10;
        Long userId = 2L;
        Pageable pageable = FromSizeRequest.of(from, size);

        List<ItemRequest> itemRequests = makeItemRequests().stream()
                .filter(itemRequest -> !itemRequest.getRequester().getId().equals(userId))
                .collect(Collectors.toList());

        when(itemRequestRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(makeItemRequests()));

        when(itemService.findItemsByRequestId(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Long id = invocationOnMock.getArgument(0);

                    return makeItems().stream()
                            .filter(itemDto -> itemDto.getRequestId().equals(id))
                            .collect(Collectors.toList());
                });

        List<ItemRequestDto> allRequests = itemRequestService.findAllRequests(from, size, userId);

        assertEquals(itemRequests.size(), allRequests.size());
        for (int i = 0; i < itemRequests.size(); i++) {
            assertEquals(itemRequests.get(i).getId(), allRequests.get(i).getId());
        }
        verify(itemRequestRepository, times(1)).findAll(any(Pageable.class));
        verify(itemService, atLeastOnce()).findItemsByRequestId(any(Long.class));
    }

    @Test
    void shouldFindRequestByIdWhenRequestExists() {
        Long requestId = 1L;
        Long userId = 1L;

        ItemRequest itemRequest = makeItemRequests().get(0);

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));

        ItemRequestDto result = itemRequestService.findRequestById(requestId, userId);

        assertEquals(requestId, result.getId());
        verify(userService, times(1)).findUserById(userId);
        verify(itemService, times(1)).findItemsByRequestId(requestId);
    }

    @Test
    void shouldThrowExceptionWhenRequestDoesNotExist() {
        Long requestId = 999L;
        Long userId = 1L;
        String expectedMessage = "Запрос с id " + requestId + " не найден!";

        Exception exception = assertThrows(
                EntityNotFoundException.class, () -> itemRequestService.findRequestById(requestId, userId)
        );

        assertEquals(expectedMessage, exception.getMessage());
        verify(userService, times(1)).findUserById(userId);
        verify(itemService, times(1)).findItemsByRequestId(requestId);
        verify(itemRequestRepository, times(1)).findById(requestId);
    }

    @Test
    void shouldThrowExceptionWhenRequestIdIsNull() {
        ItemRequest itemRequest = new ItemRequest();
        Long requestId = itemRequest.getId();
        Long userId = 1L;
        String expectedMessage = "Запрос с id " + requestId + " не найден!";

        Exception exception = assertThrows(
                ValidationException.class, () -> itemRequestService.findRequestById(requestId, userId)
        );

        assertEquals(expectedMessage, exception.getMessage());
        verify(userService, times(1)).findUserById(userId);
        verify(itemService, times(1)).findItemsByRequestId(requestId);
        verify(itemRequestRepository, times(0)).findById(requestId);
    }

    private ItemRequestShortDto makeItemRequestShortDto() {
        ItemRequestShortDto itemRequestShortDto = new ItemRequestShortDto();
        itemRequestShortDto.setId(1L);
        itemRequestShortDto.setDescription("request on itemOne");
        itemRequestShortDto.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return itemRequestShortDto;
    }

    private UserDto makeUser(Long id, String name, String email) {
        UserDto user = new UserDto();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private List<ItemDto> makeItems() {
        return List.of(
                new ItemDto(1L, "itemOne", "itemOne description", true, 1L),
                new ItemDto(2L, "itemTwo", "itemTwo description", true, 2L),
                new ItemDto(3L, "itemThree", "itemThree description", true, 3L),
                new ItemDto(4L, "itemFour", "itemFour description", true, 2L),
                new ItemDto(5L, "itemFive", "itemFive description", true, 4L)
        );
    }

    private List<ItemRequest> makeItemRequests() {
        LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        User userOne = new User(1L, "userOne", "userOne@email.com");
        User userTwo = new User(2L, "userTwo", "userTwo@email.com");
        User userThree = new User(3L, "userThree", "userThree@email.com");

        return List.of(
                new ItemRequest(1L, "requestOne description", userOne, created.minusDays(3L)),
                new ItemRequest(2L, "requestTwo description", userTwo, created.minusDays(4L)),
                new ItemRequest(3L, "requestThree description", userThree, created.minusDays(5L)),
                new ItemRequest(4L, "requestFour description", userTwo, created.minusDays(6L))
        );
    }
}
