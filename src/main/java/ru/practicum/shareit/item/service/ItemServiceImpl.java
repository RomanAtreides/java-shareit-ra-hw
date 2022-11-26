package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.utility.FromSizeRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;

    // Добавление новой вещи
    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        final Item item = getFilledItem(itemDto, userId);

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    // Добавление нового комментария
    @Override
    @Transactional
    public CommentDto createComment(CommentShortDto commentShortDto, Long itemId, Long userId) {
        checkIfUserCanAddComment(itemId, userId);

        Item item = getItemIfExists(itemId);
        User user = UserMapper.toUser(userService.findUserById(userId));
        Comment comment = ItemMapper.toComment(commentShortDto, item, user);

        return ItemMapper.toCommentDto(commentRepository.save(comment), user);
    }

    // Просмотр информации о конкретной вещи по её идентификатору
    @Override
    public ItemInfoDto findItemById(Long itemId, Long userId) {
        Item item = getItemIfExists(itemId);

        return ItemMapper.toItemInfoDto(
                item,
                bookingRepository.findLastOwnerBooking(
                                item.getId(), userId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                        ).stream()
                        .min(Comparator.comparing(Booking::getEnd))
                        .orElse(null),
                bookingRepository.findNextOwnerBooking(
                                item.getId(), userId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                        ).stream()
                        .max(Comparator.comparing(Booking::getStart))
                        .orElse(null),
                commentRepository.findCommentsByItemId(itemId).stream()
                        .map(comment -> ItemMapper.toCommentDto(comment, comment.getAuthor()))
                        .collect(Collectors.toList())
        );
    }

    // Просмотр владельцем списка всех его вещей
    @Override
    public List<ItemInfoDto> findAllUserItems(Long userId, Integer from, Integer size) {
        Pageable pageable = FromSizeRequest.of(from, size);
        List<Item> items = itemRepository.findItemsByOwnerIdOrderByIdAsc(userId, pageable);

        return items.stream()
                .map(item -> ItemMapper.toItemInfoDto(
                        item,
                        bookingRepository.findLastBooking(
                                        item.getId(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                                ).stream()
                                .min(Comparator.comparing(Booking::getEnd))
                                .orElse(null),
                        bookingRepository.findNextBooking(
                                        item.getId(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                                ).stream()
                                .max(Comparator.comparing(Booking::getStart))
                                .orElse(null),
                        commentRepository.findCommentsByItemId(item.getId()).stream()
                                .map(comment -> ItemMapper.toCommentDto(comment, comment.getAuthor()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    // Поиск вещи по имени или описанию
    @Override
    public List<ItemDto> findItemsByNameOrDescription(String text, Integer from, Integer size) {
        Pageable pageable = FromSizeRequest.of(from, size);

        if (text.isBlank()) {
            return new ArrayList<>();
        }

        String word = text.toLowerCase();

        return itemRepository.findItemsByNameOrDescription(word, pageable).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    // Редактирование вещи
    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, Long userId) {
        final Item itemToUpdate = getItemIfExists(itemDto.getId());

        checkOwner(userId, itemToUpdate);

        final Item item = getFilledItem(itemDto, userId);
        String name = item.getName();
        String description = item.getDescription();
        Boolean isAvailable = item.getAvailable();

        if (name != null) {
            itemToUpdate.setName(name);
        }

        if (description != null && !description.isBlank()) {
            itemToUpdate.setDescription(description);
        }

        if (isAvailable != null) {
            itemToUpdate.setAvailable(isAvailable);
        }
        itemRepository.save(itemToUpdate);
        return ItemMapper.toItemDto(itemToUpdate);
    }

    // Поиск владельца по идентификатору вещи
    @Override
    public User findOwnerByItemId(Long itemId) {
        return getItemIfExists(itemId).getOwner();
    }

    @Override
    public List<ItemDto> findItemsByRequestId(Long requestId) {
        return itemRepository.findItemsByRequestId(requestId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Integer findCountOfUserItems(Long userId) {
        return itemRepository.findCountOfUserItems(userId);
    }

    // Метод проверяет, является ли пользователь владельцем вещи
    private void checkOwner(Long userId, Item itemToUpdate) {
        if (userId != null && !itemToUpdate.getOwner().getId().equals(userId)) {
            throw new EntityNotFoundException(
                    "У пользователя " + userId + " предмет " + itemToUpdate.getName() + " не найден!"
            );
        }
    }

    private Item getItemIfExists(Long itemId) {
        String exceptionMessage = "Предмет с id " + itemId + " не найден!";

        if (itemId == null) {
            throw new ValidationException(exceptionMessage);
        }
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(exceptionMessage));
    }

    // Метод проверяет, может ли пользователь оставить комментарий
    private void checkIfUserCanAddComment(Long itemId, Long userId) {
        Long bookingsCount = bookingRepository.getCountOfUserBookingsWithEndIsBefore(
                userId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        if (bookingsCount == null) {
            throw new ValidationException("Нельзя оставить комментарий к предмету " + itemId + "!");
        }
    }

    private ItemRequest checkIfItemRequestIsNull(Long requestId) {
        if (requestId == null) {
            return null;
        }
        return itemRequestRepository.findItemRequestById(requestId);
    }

    private Item getFilledItem(ItemDto itemDto, Long userId) {
        final User owner = UserMapper.toUser(userService.findUserById(userId));
        final ItemRequest request = checkIfItemRequestIsNull(itemDto.getRequestId());

        return ItemMapper.toItem(itemDto, owner, request);
    }
}
