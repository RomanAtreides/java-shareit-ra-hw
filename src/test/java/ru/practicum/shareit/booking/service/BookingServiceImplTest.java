package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class BookingServiceImplTest {

    private final EntityManager em;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final Integer from = 0;
    private final Integer size = 10;
    private BookingService bookingService;
    private LocalDateTime date;
    private Booking bookingOne;
    private Booking bookingTwo;
    private Booking bookingThree;
    private Booking bookingFour;
    private User userOne;
    private User userTwo;
    private Item itemOne;
    private Item itemTwo;

    @BeforeEach
    void setUp() {
        date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        bookingService = new BookingServiceImpl(bookingRepository, userService, itemService);
        makeEntities();
    }

    @Test
    void shouldCreateBooking() {
        LocalDateTime date = LocalDateTime.now();
        BookingDto bookingDto = makeBookingDto(date.plusDays(2L), date.plusDays(3L), itemOne.getId());

        BookingInfoDto bookingInfoDto = bookingService.createBooking(bookingDto, userTwo.getId());
        TypedQuery<Booking> query = em.createQuery("select b from Booking b where b.id = :id", Booking.class);
        Booking booking = query.setParameter("id", bookingInfoDto.getId()).getSingleResult();

        assertThat(booking.getId(), equalTo(bookingInfoDto.getId()));
        assertThat(booking.getStart(), equalTo(bookingInfoDto.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingInfoDto.getEnd()));
        assertThat(booking.getBooker().getId(), equalTo(bookingInfoDto.getBooker().getId()));
        assertThat(booking.getStatus(), equalTo(bookingInfoDto.getStatus()));
    }

    @Test
    void shouldThrowExceptionWhenItemIsNotAvailable() {
        Long itemId = itemTwo.getId();
        BookingDto bookingDto = makeBookingDto(date.plusDays(2L), date.plusDays(3L), itemId);
        String expectedMessage = "Предмет с id " + itemId + " недоступен для бронирования!";

        Exception exception = assertThrows(
                ValidationException.class, () -> bookingService.createBooking(bookingDto, userTwo.getId())
        );

        assertThat(expectedMessage, equalTo(exception.getMessage()));
    }

    @Test
    void shouldThrowExceptionWhenStartIsAfterEnd() {
        String expectedMessage = "Даты указаны неверно!";
        BookingDto bookingDto = makeBookingDto(date.plusDays(2L), date.plusDays(1L), itemOne.getId());

        Exception exception = assertThrows(
                ValidationException.class, () -> bookingService.createBooking(bookingDto, userTwo.getId())
        );

        assertThat(expectedMessage, equalTo(exception.getMessage()));
    }

    @Test
    void shouldThrowExceptionWhenStartIsBeforeNow() {
        String expectedMessage = "Даты указаны неверно!";
        BookingDto bookingDto = makeBookingDto(date.minusDays(1L), date.plusDays(1L), itemOne.getId());

        Exception exception = assertThrows(
                ValidationException.class, () -> bookingService.createBooking(bookingDto, userTwo.getId())
        );

        assertThat(expectedMessage, equalTo(exception.getMessage()));
    }

    @Test
    void shouldThrowExceptionWhenUserIsOwner() {
        Long itemId = itemOne.getId();
        String expectedMessage = "Предмет с id " + itemId + " недоступен для бронирования!";
        BookingDto bookingDto = makeBookingDto(date.plusDays(1L), date.plusDays(2L), itemId);

        Exception exception = assertThrows(
                EntityNotFoundException.class, () -> bookingService.createBooking(bookingDto, userOne.getId())
        );

        assertThat(expectedMessage, equalTo(exception.getMessage()));
    }

    @Test
    void shouldFindBookingByIdWhenUserIsBooker() {
        Booking booking = makeBooking(date.plusDays(3L), date.plusDays(4L), itemOne, userTwo, BookingStatus.WAITING);

        BookingInfoDto bookingInfoDto = bookingService.findBookingById(booking.getId(), userTwo.getId());

        assertNotNull(bookingInfoDto);
        assertEquals(booking.getId(), bookingInfoDto.getId());
        assertEquals(booking.getStart(), bookingInfoDto.getStart());
        assertEquals(booking.getEnd(), bookingInfoDto.getEnd());
        assertEquals(booking.getBooker().getId(), bookingInfoDto.getBooker().getId());
        assertEquals(booking.getStatus(), bookingInfoDto.getStatus());
    }

    @Test
    void shouldFindBookingByIdWhenUserIsOwner() {
        Booking booking = makeBooking(date.plusDays(3L), date.plusDays(4L), itemOne, userTwo, BookingStatus.WAITING);

        BookingInfoDto bookingInfoDto = bookingService.findBookingById(booking.getId(), userOne.getId());

        assertNotNull(bookingInfoDto);
        assertEquals(booking.getId(), bookingInfoDto.getId());
        assertEquals(booking.getStart(), bookingInfoDto.getStart());
        assertEquals(booking.getEnd(), bookingInfoDto.getEnd());
        assertEquals(booking.getBooker().getId(), bookingInfoDto.getBooker().getId());
        assertEquals(booking.getStatus(), bookingInfoDto.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotBookerOrIsNotOwner() {
        Long userId = 999L;
        Booking booking = makeBooking(date.plusDays(3L), date.plusDays(4L), itemOne, userTwo, BookingStatus.WAITING);
        Long bookingId = booking.getId();
        String expectedMessage = "Бронирование с id " + bookingId + " не найдено!";

        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.findBookingById(bookingId, userId)
        );

        assertEquals(exception.getMessage(), expectedMessage);
    }

    @Test
    void shouldFindUserBookingsWhenUserIsOwnerAndStatusIsApproved() {
        String state = "APPROVED";
        BookingStatus status = BookingStatus.from(state);
        makeBookings(status);
        Long userId = userOne.getId();

        List<BookingInfoDto> userBookings = bookingService.findUserBookings(
                userId, state, from, size, true
        );
        TypedQuery<Booking> query = em.createQuery(
                "select b from Booking b where b.item.owner.id = :ownerId and b.status = :status", Booking.class
        );
        List<Booking> result = query
                .setParameter("ownerId", userId)
                .setParameter("status", status)
                .getResultList();

        for (BookingInfoDto userBooking : userBookings) {
            assertThat(result, hasItem(allOf(
                    hasProperty("id", equalTo(userBooking.getId())),
                    hasProperty("start", equalTo(userBooking.getStart())),
                    hasProperty("end", equalTo(userBooking.getEnd())),
                    hasProperty("status", equalTo(userBooking.getStatus()))
            )));
        }
    }

    @Test
    void shouldFindUserBookingsWhenUserIsNotOwnerAndStatusIsWaiting() {
        String state = "WAITING";
        BookingStatus status = BookingStatus.from(state);
        makeBookings(status);
        Long userId = userOne.getId();

        List<BookingInfoDto> userBookings = bookingService.findUserBookings(
                userId, state, from, size, false
        );
        TypedQuery<Booking> query = em.createQuery(
                "select b from Booking b where b.booker.id = :bookerId and b.status = :status", Booking.class
        );
        List<Booking> result = query
                .setParameter("bookerId", userId)
                .setParameter("status", status)
                .getResultList();

        for (BookingInfoDto userBooking : userBookings) {
            assertThat(result, hasItem(allOf(
                    hasProperty("id", equalTo(userBooking.getId())),
                    hasProperty("start", equalTo(userBooking.getStart())),
                    hasProperty("end", equalTo(userBooking.getEnd())),
                    hasProperty("status", equalTo(userBooking.getStatus()))
            )));
        }
    }

    @Test
    void shouldFindUserBookingsWhenUserIsOwnerAndStatusIsFuture() {
        String state = "FUTURE";
        BookingStatus status = BookingStatus.from(state);
        makeBookings(status);
        Long userId = userOne.getId();

        List<BookingInfoDto> userBookings = bookingService.findUserBookings(
                userId, state, from, size, true
        );
        TypedQuery<Booking> query = em.createQuery(
                "select b from Booking b where b.item.owner.id = :ownerId and b.start > :date order by b.id desc",
                Booking.class
        );
        List<Booking> result = query
                .setParameter("ownerId", userId)
                .setParameter("date", date)
                .getResultList();

        for (BookingInfoDto userBooking : userBookings) {
            assertThat(result, hasItem(allOf(
                    hasProperty("id", equalTo(userBooking.getId())),
                    hasProperty("start", equalTo(userBooking.getStart())),
                    hasProperty("end", equalTo(userBooking.getEnd())),
                    hasProperty("status", equalTo(userBooking.getStatus()))
            )));
            assertThat(userBooking.getStart(), greaterThan(date));
            assertThat(userBooking.getEnd(), greaterThan(userBooking.getStart()));
        }
    }

    @Test
    void shouldFindUserBookingsWhenUserIsNotOwnerAndStatusIsFuture() {
        String state = "FUTURE";
        BookingStatus status = BookingStatus.from(state);
        makeBookings(status);
        Long userId = userOne.getId();

        List<BookingInfoDto> userBookings = bookingService.findUserBookings(
                userId, state, from, size, false
        );
        TypedQuery<Booking> query = em.createQuery(
                "select b from Booking b where b.booker.id = :bookerId and b.start > :date order by b.id desc",
                Booking.class
        );
        List<Booking> result = query
                .setParameter("bookerId", userId)
                .setParameter("date", date)
                .getResultList();

        for (BookingInfoDto userBooking : userBookings) {
            assertThat(result, hasItem(allOf(
                    hasProperty("id", equalTo(userBooking.getId())),
                    hasProperty("start", equalTo(userBooking.getStart())),
                    hasProperty("end", equalTo(userBooking.getEnd())),
                    hasProperty("status", equalTo(userBooking.getStatus()))
            )));
            assertThat(userBooking.getStart(), greaterThan(date));
            assertThat(userBooking.getEnd(), greaterThan(userBooking.getStart()));
        }
    }

    @Test
    void shouldFindUserBookingsWhenUserIsOwnerAndStatusIsCurrent() {
        String state = "CURRENT";
        BookingStatus status = BookingStatus.from(state);
        makeBookings(status);
        Long userId = userOne.getId();

        List<BookingInfoDto> userBookings = bookingService.findUserBookings(
                userId, state, from, size, true
        );
        TypedQuery<Booking> query = em.createQuery(
                "select b from Booking b where b.item.owner.id = :ownerId and b.start < :date and b.end > :date",
                Booking.class
        );
        List<Booking> result = query
                .setParameter("ownerId", userId)
                .setParameter("date", date)
                .getResultList();

        for (BookingInfoDto userBooking : userBookings) {
            assertThat(result, hasItem(allOf(
                    hasProperty("id", equalTo(userBooking.getId())),
                    hasProperty("start", equalTo(userBooking.getStart())),
                    hasProperty("end", equalTo(userBooking.getEnd())),
                    hasProperty("status", equalTo(userBooking.getStatus()))
            )));
            assertThat(userBooking.getStart(), lessThan(date));
            assertThat(userBooking.getEnd(), greaterThan(userBooking.getStart()));
        }
    }

    @Test
    void shouldFindUserBookingsWhenUserIsNotOwnerAndStatusIsCurrent() {
        String state = "CURRENT";
        BookingStatus status = BookingStatus.from(state);
        makeBookings(status);
        Long userId = userOne.getId();

        List<BookingInfoDto> userBookings = bookingService.findUserBookings(
                userId, state, from, size, false
        );
        TypedQuery<Booking> query = em.createQuery(
                "select b from Booking b where b.booker.id = :bookerId and b.start < :date and b.end > :date",
                Booking.class
        );
        List<Booking> result = query
                .setParameter("bookerId", userId)
                .setParameter("date", date)
                .getResultList();

        for (BookingInfoDto userBooking : userBookings) {
            assertThat(result, hasItem(allOf(
                    hasProperty("id", equalTo(userBooking.getId())),
                    hasProperty("start", equalTo(userBooking.getStart())),
                    hasProperty("end", equalTo(userBooking.getEnd())),
                    hasProperty("status", equalTo(userBooking.getStatus()))
            )));
            assertThat(userBooking.getStart(), lessThan(date));
            assertThat(userBooking.getEnd(), greaterThan(userBooking.getStart()));
        }
    }

    @Test
    void shouldFindUserBookingsWhenUserIsOwnerAndStatusIsPast() {
        String state = "PAST";
        BookingStatus status = BookingStatus.from(state);
        makeBookings(status);
        Long userId = userOne.getId();

        List<BookingInfoDto> userBookings = bookingService.findUserBookings(
                userId, state, from, size, true
        );
        TypedQuery<Booking> query = em.createQuery(
                "select b from Booking b where b.item.owner.id = :ownerId and b.end < :date order by b.id desc",
                Booking.class
        );
        List<Booking> result = query
                .setParameter("ownerId", userId)
                .setParameter("date", date)
                .getResultList();

        for (BookingInfoDto userBooking : userBookings) {
            assertThat(result, hasItem(allOf(
                    hasProperty("id", equalTo(userBooking.getId())),
                    hasProperty("start", equalTo(userBooking.getStart())),
                    hasProperty("end", equalTo(userBooking.getEnd())),
                    hasProperty("status", equalTo(userBooking.getStatus()))
            )));
            assertThat(userBooking.getEnd(), lessThan(date));
        }
    }

    @Test
    void shouldFindUserBookingsWhenUserIsNotOwnerAndStatusIsPast() {
        String state = "PAST";
        BookingStatus status = BookingStatus.from(state);
        makeBookings(status);
        Long userId = userOne.getId();

        List<BookingInfoDto> userBookings = bookingService.findUserBookings(
                userId, state, from, size, false
        );
        TypedQuery<Booking> query = em.createQuery(
                "select b from Booking b where b.booker.id = :bookerId and b.end < :date order by b.id desc",
                Booking.class
        );
        List<Booking> result = query
                .setParameter("bookerId", userId)
                .setParameter("date", date)
                .getResultList();

        for (BookingInfoDto userBooking : userBookings) {
            assertThat(result, hasItem(allOf(
                    hasProperty("id", equalTo(userBooking.getId())),
                    hasProperty("start", equalTo(userBooking.getStart())),
                    hasProperty("end", equalTo(userBooking.getEnd())),
                    hasProperty("status", equalTo(userBooking.getStatus()))
            )));
            assertThat(userBooking.getEnd(), lessThan(date));
        }
    }

    @Test
    void shouldFindUserBookingsWhenUserIsOwnerAndStatusIsAll() {
        String state = "ALL";
        BookingStatus status = BookingStatus.from(state);
        makeBookings(status);
        Long userId = userOne.getId();

        List<BookingInfoDto> userBookings = bookingService.findUserBookings(
                userId, state, from, size, true
        );
        TypedQuery<Booking> query = em.createQuery(
                "select b from Booking b where b.item.owner.id = :ownerId order by b.id desc",
                Booking.class
        );
        List<Booking> result = query
                .setParameter("ownerId", userId)
                .getResultList();

        for (BookingInfoDto userBooking : userBookings) {
            assertThat(result, hasItem(allOf(
                    hasProperty("id", equalTo(userBooking.getId())),
                    hasProperty("start", equalTo(userBooking.getStart())),
                    hasProperty("end", equalTo(userBooking.getEnd())),
                    hasProperty("status", equalTo(userBooking.getStatus()))
            )));
        }
    }

    @Test
    void shouldFindUserBookingsWhenUserIsNotOwnerAndStatusIsAll() {
        String state = "ALL";
        BookingStatus status = BookingStatus.from(state);
        makeBookings(status);
        Long userId = userOne.getId();

        List<BookingInfoDto> userBookings = bookingService.findUserBookings(
                userId, state, from, size, false
        );
        TypedQuery<Booking> query = em.createQuery(
                "select b from Booking b where b.booker.id = :bookerId order by b.id desc",
                Booking.class
        );
        List<Booking> result = query
                .setParameter("bookerId", userId)
                .getResultList();

        for (BookingInfoDto userBooking : userBookings) {
            assertThat(result, hasItem(allOf(
                    hasProperty("id", equalTo(userBooking.getId())),
                    hasProperty("start", equalTo(userBooking.getStart())),
                    hasProperty("end", equalTo(userBooking.getEnd())),
                    hasProperty("status", equalTo(userBooking.getStatus()))
            )));
        }
    }

    @Test
    void shouldThrowExceptionWhenBookingStatusIsNull() {
        String expectedMessage = "Unknown state: " + null;

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.findUserBookings(userOne.getId(), null, from, size, true)
        );

        assertEquals(exception.getMessage(), expectedMessage);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        Long userId = 999L;
        String stateParam = "APPROVED";
        String expectedMessage = "Пользователь с id " + userId + " не найден!";

        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.findUserBookings(userId, stateParam, from, size, true)
        );

        assertEquals(exception.getMessage(), expectedMessage);
    }

    @Test
    void shouldChangeBookingStatusWhenApprovedIsTrue() {
        Boolean approved = true;
        Booking booking = makeBooking(date.plusDays(5L), date.plusDays(6L), itemOne, userTwo, BookingStatus.WAITING);

        BookingInfoDto bookingInfoDto = bookingService
                .changeBookingStatus(approved, booking.getId(), userOne.getId());
        TypedQuery<Booking> query = em.createQuery("select b from Booking b where b.id = :id", Booking.class);
        Booking result = query.setParameter("id", bookingInfoDto.getId()).getSingleResult();

        assertThat(bookingInfoDto.getId(), equalTo(result.getId()));
        assertThat(result.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwner() {
        Boolean approved = true;
        Booking booking = makeBooking(date.plusDays(5L), date.plusDays(6L), itemOne, userTwo, BookingStatus.WAITING);
        String expectedMessage = "Бронирование с id " + booking.getId() + " не найдено!";

        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.changeBookingStatus(approved, booking.getId(), userTwo.getId())
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenBookingIsAlreadyApproved() {
        Boolean approved = true;
        Booking booking = makeBooking(date.plusDays(5L), date.plusDays(6L), itemOne, userTwo, BookingStatus.APPROVED);
        String expectedMessage = "Бронирование " + booking.getId() + " уже подтверждено!";

        Exception exception = assertThrows(
                ValidationException.class,
                () -> bookingService.changeBookingStatus(approved, booking.getId(), userOne.getId())
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldChangeBookingStatusWhenApprovedIsFalse() {
        Boolean approved = false;
        Booking booking = makeBooking(date.plusDays(5L), date.plusDays(6L), itemOne, userTwo, BookingStatus.WAITING);

        BookingInfoDto bookingInfoDto = bookingService
                .changeBookingStatus(approved, booking.getId(), userOne.getId());
        TypedQuery<Booking> query = em.createQuery("select b from Booking b where b.id = :id", Booking.class);
        Booking result = query.setParameter("id", bookingInfoDto.getId()).getSingleResult();

        assertThat(bookingInfoDto.getId(), equalTo(result.getId()));
        assertThat(result.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void shouldThrowExceptionWhenBookingDoesNotExist() {
        Long bookingId = 999L;
        makeBookings(BookingStatus.WAITING);
        List<Booking> bookings = List.of(bookingOne, bookingTwo, bookingThree, bookingFour);

        Exception exception = assertThrows(
                EntityNotFoundException.class, () -> bookingService.findBookingById(bookingId, userOne.getId())
        );
        String expectedMessage = "Бронирование с id " + bookingId + " не найдено!";
        String actualMessage = exception.getMessage();

        assertThat(bookings.size(), greaterThanOrEqualTo(1));
        assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void shouldThrowExceptionWhenBookingIdIsNull() {
        Long bookingId = null;

        Exception exception = assertThrows(
                ValidationException.class, () -> bookingService.findBookingById(bookingId, userOne.getId())
        );
        String expectedMessage = "Бронирование с id " + bookingId + " не найдено!";
        String actualMessage = exception.getMessage();

        assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void shouldThrowExceptionWhenUserHasNoItems() {
        User user = makeUser("user3", "user3@emil.com");
        Long userId = user.getId();
        String stateParam = "APPROVED";
        String expectedMessage = "У пользователя " + userId + " нет вещей!";

        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.findUserBookings(userId, stateParam, from, size, false)
        );

        assertEquals(exception.getMessage(), expectedMessage);
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        em.persist(user);
        return user;
    }

    private ItemRequest makeRequest(User requester, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription("request on item001");
        request.setRequester(requester);
        request.setCreated(created);
        em.persist(request);
        return request;
    }

    private Item makeItem(String name, String description, Boolean available, User owner, ItemRequest request) {
        Item item = Item.builder()
                .name(name)
                .description(description)
                .available(available)
                .owner(owner)
                .request(request)
                .build();
        em.persist(item);
        return item;
    }

    private Booking makeBooking(LocalDateTime start, LocalDateTime end, Item item, User booker, BookingStatus status) {
        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(status)
                .build();
        em.persist(booking);
        return booking;
    }

    private BookingDto makeBookingDto(LocalDateTime start, LocalDateTime end, Long itemId) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItemId(itemId);
        return bookingDto;
    }

    private void makeEntities() {
        userOne = makeUser("user001", "user001@email.com");
        userTwo = makeUser("user002", "user002@email.com");

        ItemRequest requestOne = makeRequest(userTwo, date.minusDays(5L));

        itemOne = makeItem("item001", "item001 description", true, userOne, requestOne);
        itemTwo = makeItem("item002", "item002 description", false, userOne, requestOne);
    }

    private void makeBookings(BookingStatus status) {
        bookingOne = makeBooking(date.plusDays(5L), date.plusDays(6L), itemOne, userTwo, status);
        bookingTwo = makeBooking(date.plusDays(2L), date.plusDays(3L), itemTwo, userTwo, status);
        bookingThree = makeBooking(date.plusDays(7L), date.plusDays(8L), itemTwo, userOne, status);
        bookingFour = makeBooking(date.plusDays(9L), date.plusDays(10L), itemOne, userTwo, BookingStatus.ALL);
    }
}
