package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.utility.FromSizeRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    // Добавление нового запроса на бронирование
    @Override
    @Transactional
    public BookingInfoDto createBooking(BookingDto bookingDto, Long bookerId) {
        Long itemId = bookingDto.getItemId();
        User owner = itemService.findOwnerByItemId(itemId);
        Long ownerId = owner.getId();
        ItemInfoDto itemInfoDto = itemService.findItemById(itemId, ownerId);
        ItemRequest request = itemInfoDto.getRequest();
        final Item item = ItemMapper.toItemFromInfoDto(itemInfoDto, owner, request);

        checkIfItemIsAvailable(item);
        checkIfDatesAreValid(bookingDto);
        checkIfUserIsOwner(ownerId, bookerId, item);

        final User booker = UserMapper.toUser(userService.findUserById(bookerId));
        final Booking booking = BookingMapper.toBooking(bookingDto, item, booker);

        return BookingMapper.toBookingInfoDto(bookingRepository.save(booking));
    }

    // Получение данных о конкретном бронировании (включая его статус)
    @Override
    public BookingInfoDto findBookingById(Long bookingId, Long userId) {
        final Booking booking = getBookingIfExists(bookingId);

        checkUserIsBookerOrIsOwner(booking, bookingId, userId);
        return BookingMapper.toBookingInfoDto(booking);
    }

    // Получение списка всех бронирований текущего пользователя
    @Override
    public List<BookingInfoDto> findUserBookings(
            Long userId,
            String stateParam,
            Integer from,
            Integer size,
            boolean isOwner) {
        Pageable pageable = FromSizeRequest.of(from, size);
        BookingStatus status = checkBookingStatus(stateParam);

        checkIfUserIsExists(userId);
        checkIfUserHasItems(userId);
        return selectBookings(userId, status, pageable, isOwner).stream()
                .map(BookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    // Подтверждение или отклонение запроса на бронирование
    @Override
    @Transactional
    public BookingInfoDto changeBookingStatus(Boolean approved, Long bookingId, Long userId) {
        final Booking booking = getBookingIfExists(bookingId);

        checkIfUserIsOwner(booking, bookingId, userId);
        checkIfBookingIsAlreadyApproved(booking, bookingId);
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingInfoDto(bookingRepository.save(booking));
    }

    private List<Booking> selectBookings(Long userId, BookingStatus status, Pageable pageable, boolean isOwner) {
        List<Booking> bookings;

        switch (status) {
            case FUTURE:
                bookings = isOwner ?
                        bookingRepository.findOwnerBookingsWithStartIsAfter(
                                userId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), pageable) :
                        bookingRepository.findUserBookingsWithStartIsAfter(
                                userId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), pageable);
                break;
            case CURRENT:
                bookings = isOwner ?
                        bookingRepository.findCurrentOwnerBookings(
                                userId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), pageable) :
                        bookingRepository.findCurrentUserBookings(
                                userId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), pageable);
                break;
            case PAST:
                bookings = isOwner ?
                        bookingRepository.findOwnerBookingsWithEndIsBefore(
                                userId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), pageable) :
                        bookingRepository.findUserBookingsWithEndIsBefore(
                                userId, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), pageable);
                break;
            case WAITING:
            case APPROVED:
            case REJECTED:
            case CANCELED:
                bookings = isOwner ?
                        bookingRepository.findOwnerBookingsByState(userId, status, pageable) :
                        bookingRepository.findUserBookingsByState(userId, status, pageable);
                break;
            default:
                bookings = isOwner ?
                        bookingRepository.findOwnerBookings(userId, pageable) :
                        bookingRepository.findUserBookings(userId, pageable);
                break;
        }
        return bookings;
    }

    private void checkIfItemIsAvailable(Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("Предмет с id " + item.getId() + " недоступен для бронирования!");
        }
    }

    private void checkIfDatesAreValid(BookingDto bookingDto) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        boolean isValid = start.isAfter(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)) && start.isBefore(end);

        if (!isValid) {
            throw new ValidationException("Даты указаны неверно!");
        }
    }

    private void checkIfUserIsOwner(Long ownerId, Long bookerId, Item item) {
        if (ownerId.equals(bookerId)) {
            throw new EntityNotFoundException("Предмет с id " + item.getId() + " недоступен для бронирования!");
        }
    }

    private Booking getBookingIfExists(Long bookingId) {
        String exceptionMessage = "Бронирование с id " + bookingId + " не найдено!";

        if (bookingId == null) {
            throw new ValidationException(exceptionMessage);
        }
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(exceptionMessage));
    }

    // Метод проверяет, что информацию о бронировании пытается получить или автор бронирования, или владелец вещи
    private void checkUserIsBookerOrIsOwner(Booking booking, Long bookingId, Long userId) {
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();

        if (!userId.equals(bookerId) && !userId.equals(ownerId)) {
            throw new EntityNotFoundException("Бронирование с id " + bookingId + " не найдено!");
        }
    }

    private BookingStatus checkBookingStatus(String stateParam) {
        BookingStatus status = BookingStatus.from(stateParam);

        if (status == null) {
            throw new IllegalArgumentException("Unknown state: " + stateParam);
        }
        return status;
    }

    private void checkIfUserIsExists(Long userId) {
        userService.findUserById(userId);
    }

    private void checkIfUserHasItems(Long userId) {
        if (itemService.findCountOfUserItems(userId) == 0) {
            throw new EntityNotFoundException("У пользователя " + userId + " нет вещей!");
        }
    }

    private void checkIfUserIsOwner(Booking booking, Long bookingId, Long userId) {
        Long ownerId = booking.getItem().getOwner().getId();

        if (!ownerId.equals(userId)) {
            throw new EntityNotFoundException("Бронирование с id " + bookingId + " не найдено!");
        }
    }

    private void checkIfBookingIsAlreadyApproved(Booking booking, Long bookingId) {
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ValidationException("Бронирование " + bookingId + " уже подтверждено!");
        }
    }
}
