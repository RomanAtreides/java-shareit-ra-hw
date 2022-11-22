package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    private final LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item itemOne;
    private Item itemTwo;
    private Booking bookingOne;
    private Booking bookingTwo;
    private Booking bookingThree;
    private Booking bookingFour;
    private BookingStatus status;
    private int expectedAmountOfBookings;

    @BeforeEach
    void setUp() {
        owner = createUser("owner", "owner@email.com");
        booker = createUser("booker", "booker@email.com");

        ItemRequest requestOne = createRequest("requestOne description", booker, date.minusDays(15L));
        ItemRequest requestTwo = createRequest("requestTwo description", booker, date.minusDays(13L));

        itemOne = createItem("itemOne", "itemOne description", true, owner, requestOne);
        itemTwo = createItem("itemTwo", "itemTwo description", false, owner, requestTwo);

        bookingOne = createBooking(date.minusDays(6L), date.minusDays(5L), itemOne, booker, BookingStatus.PAST);
        bookingTwo = createBooking(date.minusDays(4L), date.minusDays(3L), itemOne, booker, BookingStatus.PAST);
        bookingThree = createBooking(date.minusDays(2L), date.plusDays(1L), itemTwo, booker, BookingStatus.CURRENT);
        bookingFour = createBooking(date.plusDays(2L), date.plusDays(3L), itemTwo, booker, BookingStatus.WAITING);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        itemRequestRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void findLastBooking() {
        expectedAmountOfBookings = 2;
        Long itemOneId = itemOne.getId();

        List<Booking> result = bookingRepository.findLastBooking(itemOneId, date);

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(itemOneId, result.get(0).getItem().getId());
        assertEquals(itemOneId, result.get(1).getItem().getId());
        assertTrue(result.get(0).getStart().isBefore(date));
        assertTrue(result.get(1).getStart().isBefore(date));
    }

    @Test
    void findNextBooking() {
        expectedAmountOfBookings = 1;
        Long itemTwoId = itemTwo.getId();

        List<Booking> result = bookingRepository.findNextBooking(itemTwoId, date);

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(itemTwoId, result.get(0).getItem().getId());
        assertTrue(result.get(0).getStart().isAfter(date));
    }

    @Test
    void findOwnerBookingsWithStartIsAfter() {
        expectedAmountOfBookings = 1;
        Long ownerId = owner.getId();

        List<Booking> result = bookingRepository
                .findOwnerBookingsWithStartIsAfter(ownerId, date, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(bookingFour.getId(), result.get(0).getId());
        assertEquals(ownerId, result.get(0).getItem().getOwner().getId());
        assertTrue(result.get(0).getStart().isAfter(date));
    }

    @Test
    void findUserBookingsWithStartIsAfter() {
        expectedAmountOfBookings = 1;
        Long bookerId = booker.getId();

        List<Booking> result = bookingRepository
                .findUserBookingsWithStartIsAfter(bookerId, date, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(bookingFour.getId(), result.get(0).getId());
        assertEquals(bookerId, result.get(0).getBooker().getId());
        assertTrue(result.get(0).getStart().isAfter(date));
    }

    @Test
    void findCurrentOwnerBookings() {
        expectedAmountOfBookings = 1;
        Long ownerId = owner.getId();

        List<Booking> result = bookingRepository
                .findCurrentOwnerBookings(ownerId, date, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(bookingThree.getId(), result.get(0).getId());
        assertEquals(ownerId, result.get(0).getItem().getOwner().getId());
        assertTrue(result.get(0).getStart().isBefore(date));
        assertTrue(result.get(0).getEnd().isAfter(date));
    }

    @Test
    void findCurrentUserBookings() {
        expectedAmountOfBookings = 1;
        Long bookerId = booker.getId();

        List<Booking> result = bookingRepository
                .findCurrentUserBookings(bookerId, date, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(bookingThree.getId(), result.get(0).getId());
        assertEquals(bookerId, result.get(0).getBooker().getId());
        assertTrue(result.get(0).getStart().isBefore(date));
        assertTrue(result.get(0).getEnd().isAfter(date));
    }

    @Test
    void findOwnerBookingsWithEndIsBefore() {
        expectedAmountOfBookings = 2;
        Long ownerId = owner.getId();

        List<Booking> result = bookingRepository
                .findOwnerBookingsWithEndIsBefore(ownerId, date, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(ownerId, result.get(0).getItem().getOwner().getId());
        assertEquals(ownerId, result.get(1).getItem().getOwner().getId());
        assertTrue(result.get(0).getEnd().isBefore(date));
        assertTrue(result.get(1).getEnd().isBefore(date));
        assertEquals(bookingOne.getId(), result.get(1).getId());
        assertEquals(bookingTwo.getId(), result.get(0).getId());
    }

    @Test
    void findUserBookingsWithEndIsBefore() {
        expectedAmountOfBookings = 2;
        Long bookerId = booker.getId();

        List<Booking> result = bookingRepository
                .findUserBookingsWithEndIsBefore(bookerId, date, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(bookerId, result.get(0).getBooker().getId());
        assertEquals(bookerId, result.get(1).getBooker().getId());
        assertTrue(result.get(0).getEnd().isBefore(date));
        assertTrue(result.get(1).getEnd().isBefore(date));
    }

    @Test
    void findOwnerBookingsByState() {
        expectedAmountOfBookings = 2;
        status = BookingStatus.PAST;
        Long ownerId = owner.getId();

        List<Booking> result = bookingRepository
                .findOwnerBookingsByState(ownerId, status, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(ownerId, result.get(0).getItem().getOwner().getId());
        assertEquals(ownerId, result.get(1).getItem().getOwner().getId());
        assertEquals(status, result.get(0).getStatus());
        assertEquals(status, result.get(1).getStatus());
    }

    @Test
    void findUserBookingsByState() {
        expectedAmountOfBookings = 1;
        status = BookingStatus.CURRENT;
        Long bookerId = booker.getId();

        List<Booking> result = bookingRepository
                .findUserBookingsByState(bookerId, status, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(bookerId, result.get(0).getBooker().getId());
        assertEquals(status, result.get(0).getStatus());
    }

    @Test
    void findOwnerBookings() {
        expectedAmountOfBookings = 4;
        Long ownerId = owner.getId();

        List<Booking> result = bookingRepository
                .findOwnerBookings(ownerId, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(ownerId, result.get(0).getItem().getOwner().getId());
        assertEquals(ownerId, result.get(1).getItem().getOwner().getId());
        assertEquals(ownerId, result.get(2).getItem().getOwner().getId());
        assertEquals(ownerId, result.get(3).getItem().getOwner().getId());
        assertTrue(result.get(0).getId() > result.get(1).getId());
        assertTrue(result.get(1).getId() > result.get(2).getId());
        assertTrue(result.get(2).getId() > result.get(3).getId());
    }

    @Test
    void findUserBookings() {
        expectedAmountOfBookings = 4;
        Long bookerId = booker.getId();

        List<Booking> result = bookingRepository
                .findUserBookings(bookerId, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(bookerId, result.get(0).getBooker().getId());
        assertEquals(bookerId, result.get(1).getBooker().getId());
        assertEquals(bookerId, result.get(2).getBooker().getId());
        assertEquals(bookerId, result.get(3).getBooker().getId());
        assertTrue(result.get(0).getId() > result.get(1).getId());
        assertTrue(result.get(1).getId() > result.get(2).getId());
        assertTrue(result.get(2).getId() > result.get(3).getId());
    }

    @Test
    void findLastOwnerBooking() {
        expectedAmountOfBookings = 2;
        Long ownerId = owner.getId();
        Long itemId = itemOne.getId();

        List<Booking> result = bookingRepository
                .findLastOwnerBooking(itemId, ownerId, date);

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(itemId, result.get(0).getItem().getId());
        assertEquals(itemId, result.get(1).getItem().getId());
        assertEquals(ownerId, result.get(0).getItem().getOwner().getId());
        assertEquals(ownerId, result.get(1).getItem().getOwner().getId());
        assertTrue(result.get(0).getEnd().isBefore(date));
        assertTrue(result.get(1).getEnd().isBefore(date));
    }

    @Test
    void findNextOwnerBooking() {
        expectedAmountOfBookings = 1;
        Long ownerId = owner.getId();
        Long itemId = itemTwo.getId();

        List<Booking> result = bookingRepository
                .findNextOwnerBooking(itemId, ownerId, date);

        assertNotNull(result);
        assertEquals(expectedAmountOfBookings, result.size());
        assertEquals(itemId, result.get(0).getItem().getId());
        assertEquals(ownerId, result.get(0).getItem().getOwner().getId());
        assertTrue(result.get(0).getStart().isAfter(date));
    }

    @Test
    void getCountOfUserBookingsWithEndIsBefore() {
        long expectedAmount = 2L;
        Long bookerId = booker.getId();

        Long result = bookingRepository
                .getCountOfUserBookingsWithEndIsBefore(bookerId, date);

        assertEquals(expectedAmount, result);
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private ItemRequest createRequest(String description, User requester, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequester(requester);
        request.setCreated(created);
        return itemRequestRepository.save(request);
    }

    private Item createItem(String name, String description, Boolean available, User owner, ItemRequest request) {
        return itemRepository.save(Item.builder()
                .name(name)
                .description(description)
                .available(available)
                .owner(owner)
                .request(request)
                .build());
    }

    private Booking createBooking(
            LocalDateTime start,
            LocalDateTime end,
            Item item,
            User booker,
            BookingStatus status) {
        return bookingRepository.save(Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(status)
                .build());
    }
}
