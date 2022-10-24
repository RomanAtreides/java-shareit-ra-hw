package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class ItemServiceImplTest {

    private final EntityManager em;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private List<Item> testItems;
    private Item itemOne;
    private Item itemTwo;
    private Item itemThree;
    private ItemRequest requestOne;
    private ItemService itemService;
    private User userOne;
    private User userTwo;
    private LocalDateTime date;

    @BeforeEach
    void setUp() {
        date = LocalDateTime.now();
        itemService = new ItemServiceImpl(
                bookingRepository, commentRepository, itemRepository, itemRequestRepository, userService
        );

        makeEntities();
    }

    @AfterEach
    void tearDown() {
        em.createNativeQuery("truncate table items");
    }

    @Test
    void shouldCreateItem() {
        Long userId = userOne.getId();
        ItemDto itemDto = makeItemDto(
                "item013", "item013 description", true, requestOne.getId()
        );

        itemDto = itemService.createItem(itemDto, userId);

        TypedQuery<Item> query = em.createQuery("select i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertThat(item.getId(), equalTo(itemDto.getId()));
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(item.getRequest().getId(), equalTo(itemDto.getRequestId()));
        assertThat(item.getOwner().getId(), equalTo(userId));
    }

    @Test
    void shouldCreateItemWithoutRequestWhenDtoRequestIdIsNull() {
        ItemDto itemDto = makeItemDto(
                "item023", "item023 description", true, null
        );

        itemDto = itemService.createItem(itemDto, userOne.getId());
        TypedQuery<Item> query = em.createQuery("select i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertThat(item.getId(), equalTo(itemDto.getId()));
        assertThat(itemDto.getRequestId(), equalTo(null));
        assertThat(item.getRequest(), equalTo(null));
    }

    @Test
    void shouldCreateComment() {
        Booking bookingOne = makeBooking(
                date.minusDays(3L), date.minusDays(2L), itemOne, userTwo, BookingStatus.APPROVED
        );
        Booking bookingTwo = makeBooking(
                date.minusDays(1L), date.plusMinutes(1L), itemTwo, userTwo, BookingStatus.CURRENT
        );
        CommentShortDto commentShortDto = makeCommentShortDto("comment003", LocalDateTime.now());
        CommentDto commentDto = itemService.createComment(commentShortDto, itemOne.getId(), userTwo.getId());

        TypedQuery<Comment> query = em.createQuery("select c from Comment c where c.id = :id", Comment.class);
        Comment comment = query.setParameter("id", commentDto.getId()).getSingleResult();

        assertThat(comment.getId(), equalTo(commentDto.getId()));
        assertThat(comment.getText(), equalTo(commentDto.getText()));
        assertThat(comment.getAuthor().getName(), equalTo(commentDto.getAuthorName()));
        assertThat(comment.getAuthor().getId(), equalTo(bookingOne.getBooker().getId()));
        assertThat(comment.getAuthor().getId(), equalTo(bookingTwo.getBooker().getId()));
        assertThat(comment.getCreated(), equalTo(commentDto.getCreated()));
    }

    @Test
    void shouldThrowExceptionWhenItemHasNoBookings() {
        Long itemId = itemOne.getId();
        CommentShortDto commentShortDto = makeCommentShortDto("comment404", LocalDateTime.now());

        Exception exception = assertThrows(
                ValidationException.class, () -> itemService.createComment(commentShortDto, itemId, userOne.getId())
        );
        String expectedMessage = "Нельзя оставить комментарий к предмету " + itemId + "!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldFindItemById() {
        Comment comment = makeComment("comment about the itemThree", itemThree, userTwo, date);

        ItemInfoDto result = itemService.findItemById(itemThree.getId(), itemThree.getOwner().getId());

        assertNotNull(result);
        assertEquals(itemThree.getId(), result.getId());
        assertEquals(itemThree.getName(), result.getName());
        assertEquals(itemThree.getDescription(), result.getDescription());
        assertEquals(itemThree.getAvailable(), result.getAvailable());
        assertEquals(itemThree.getRequest(), result.getRequest());
        assertTrue(result.getComments().stream()
                .anyMatch(commentDto -> commentDto.getId().equals(comment.getId()))
        );
    }

    @Test
    void shouldThrowExceptionWhenItemDoesNotExist() {
        Long itemId = 999L;

        Exception exception = assertThrows(
                EntityNotFoundException.class, () -> itemService.findItemById(itemId, userOne.getId())
        );
        String expectedMessage = "Предмет с id " + itemId + " не найден!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldFindAllUserItems() {
        Long userId = userOne.getId();
        Integer from = 0;
        Integer size = 10;
        Comment comment = makeComment("comment about the itemTwo", itemTwo, userOne, date);

        List<ItemInfoDto> items = itemService.findAllUserItems(userId, from, size);
        TypedQuery<Item> query = em.createQuery("select i from Item i where i.owner.id = :id", Item.class);
        List<Item> result = query.setParameter("id", userId).getResultList();

        assertTrue(
                items.stream()
                        .anyMatch(itemInfoDto -> itemInfoDto.getComments().stream()
                                .anyMatch(commentDto -> commentDto.getId().equals(comment.getId())))
        );
        assertThat(items, notNullValue());
        assertThat(result, hasSize(items.size()));

        for (ItemInfoDto item : items) {
            assertThat(result, hasItem(allOf(
                    hasProperty("id", equalTo(item.getId())),
                    hasProperty("name", equalTo(item.getName())),
                    hasProperty("description", equalTo(item.getDescription())),
                    hasProperty("available", equalTo(item.getAvailable())),
                    hasProperty("request", equalTo(item.getRequest()))
            )));
        }
    }

    @Test
    void shouldFindItemsByNameOrDescription() {
        String text = "item";
        Integer from = 0; // индекс первого элемента, начиная с 0
        Integer size = 10; // количество элементов для отображения

        List<ItemDto> items = itemService.findItemsByNameOrDescription(text, from, size);

        for (ItemDto item : items) {
            assertTrue(item.getName().contains(text));
        }
    }

    @Test
    void shouldReturnEmptyListIfTextIsBlank() {
        String text = " ";
        Integer from = 0;
        Integer size = 10;

        List<ItemDto> items = itemService.findItemsByNameOrDescription(text, from, size);

        assertThat(items.size(), equalTo(0));
    }

    @Test
    void shouldUpdateItem() {
        ItemDto itemDtoOld = makeItemDto(
                "item455", "item455 description", true, requestOne.getId()
        );
        Item itemOld = ItemMapper.toItem(itemDtoOld, userTwo, requestOne);
        Item entity = em.merge(itemOld);
        Long userId = userTwo.getId();
        Long entityId = entity.getId();
        ItemDto itemDtoNew = makeItemDto(
                "item559", "item559 description", true, requestOne.getId()
        );
        itemDtoNew.setId(entityId);

        itemService.updateItem(itemDtoNew, userId);
        TypedQuery<Item> query = em.createQuery("select i from Item i where i.id = :id", Item.class);
        Item itemNew = query.setParameter("id", entityId).getSingleResult();

        assertThat(itemNew, allOf(
                hasProperty("id", equalTo(entity.getId())),
                hasProperty("name", equalTo(entity.getName())),
                hasProperty("description", equalTo(entity.getDescription())),
                hasProperty("available", equalTo(entity.getAvailable())),
                hasProperty("owner", equalTo(entity.getOwner())),
                hasProperty("request", equalTo(entity.getRequest()))
        ));
    }

    @Test
    void shouldThrowExceptionWhenItemIdIsNull() {
        Long itemId = null;
        String message = "Предмет с id " + itemId + " не найден!";
        ItemDto itemDto = ItemMapper.toItemDto(itemOne);

        itemDto.setId(itemId);
        Exception exception = assertThrows(
                ValidationException.class, () -> itemService.updateItem(itemDto, userOne.getId())
        );

        assertThat(message, equalTo(exception.getMessage()));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwner() {
        Long userId = userTwo.getId();
        ItemDto itemDto = ItemMapper.toItemDto(itemOne);
        String message = "У пользователя " + userId + " предмет " + itemDto.getName() + " не найден!";

        Exception exception = assertThrows(
                EntityNotFoundException.class, () -> itemService.updateItem(itemDto, userId)
        );

        assertThat(message, equalTo(exception.getMessage()));
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        Long userId = null;
        ItemDto itemDto = ItemMapper.toItemDto(itemOne);
        String message = "Пользователь с id " + userId + " не найден!";

        Exception exception = assertThrows(
                ValidationException.class, () -> itemService.updateItem(itemDto, userId)
        );

        assertThat(message, equalTo(exception.getMessage()));
    }

    @Test
    void shouldHoldItemNameAndDescriptionAndAvailableWhenTheirNewValuesAreNull() {
        ItemDto itemDtoOld = makeItemDto(
                "item133", "item133 description", true, requestOne.getId()
        );
        Item itemOld = ItemMapper.toItem(itemDtoOld, userTwo, requestOne);
        Item entity = em.merge(itemOld);
        Long userId = userTwo.getId();
        Long entityId = entity.getId();
        ItemDto itemDtoNew = makeItemDto(
                null, null, null, requestOne.getId()
        );
        itemDtoNew.setId(entityId);

        itemService.updateItem(itemDtoNew, userId);
        TypedQuery<Item> query = em.createQuery("select i from Item i where i.id = :id", Item.class);
        Item itemNew = query.setParameter("id", entityId).getSingleResult();

        assertThat(itemNew, allOf(
                hasProperty("id", equalTo(entity.getId())),
                hasProperty("name", equalTo(entity.getName())),
                hasProperty("description", equalTo(entity.getDescription())),
                hasProperty("available", equalTo(entity.getAvailable()))
        ));
    }

    @Test
    void shouldHoldItemDescriptionWhenNewValueIsBlank() {
        ItemDto itemDtoOld = makeItemDto(
                "item502", "item502 description", true, requestOne.getId()
        );
        Item itemOld = ItemMapper.toItem(itemDtoOld, userTwo, requestOne);
        Item entity = em.merge(itemOld);
        Long userId = userTwo.getId();
        Long entityId = entity.getId();
        ItemDto itemDtoNew = makeItemDto("item601", " ", true, requestOne.getId());
        itemDtoNew.setId(entityId);

        itemService.updateItem(itemDtoNew, userId);
        TypedQuery<Item> query = em.createQuery("select i from Item i where i.id = :id", Item.class);
        Item itemNew = query.setParameter("id", entityId).getSingleResult();

        assertThat(itemNew, allOf(
                hasProperty("id", equalTo(entity.getId())),
                hasProperty("description", equalTo(entity.getDescription()))
        ));
    }

    @Test
    void shouldFindOwnerByItemId() {
        User owner = itemService.findOwnerByItemId(itemOne.getId());

        assertThat(owner, allOf(
                hasProperty("id", equalTo(userOne.getId())),
                hasProperty("name", equalTo(userOne.getName())),
                hasProperty("email", equalTo(userOne.getEmail()))
        ));
    }

    @Test
    void shouldFindItemsByRequestId() {
        List<Item> items = List.of(itemOne, itemTwo);

        List<ItemDto> itemRegister = itemService.findItemsByRequestId(requestOne.getId());

        assertThat(itemOne.getRequest().getId(), equalTo(requestOne.getId()));
        assertThat(itemTwo.getRequest().getId(), equalTo(requestOne.getId()));
        assertThat(itemRegister, hasSize(items.size()));
    }

    @Test
    void shouldFindCountOfUserItems() {
        Long ownerId = userOne.getId();
        List<Item> items = testItems.stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());

        Integer itemsCount = itemService.findCountOfUserItems(ownerId);

        assertThat(itemsCount, equalTo(items.size()));
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        em.persist(user);
        return user;
    }

    private ItemRequest makeRequest(String description, User requester, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequester(requester);
        request.setCreated(created);
        em.persist(request);
        return request;
    }

    private Item makeItem(String name, String description, Boolean available, User owner, ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        item.setRequest(request);
        em.persist(item);
        return item;
    }

    private Booking makeBooking(LocalDateTime start, LocalDateTime end, Item item, User booker, BookingStatus status) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        em.persist(booking);
        return booking;
    }

    private Comment makeComment(String text, Item item, User author, LocalDateTime date) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(date);
        em.persist(comment);
        return comment;
    }

    private ItemDto makeItemDto(String name, String description, Boolean available, Long requestId) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        itemDto.setRequestId(requestId);
        return itemDto;
    }

    private CommentShortDto makeCommentShortDto(String text, LocalDateTime created) {
        CommentShortDto commentShortDto = new CommentShortDto();
        commentShortDto.setText(text);
        commentShortDto.setCreated(created);
        return commentShortDto;
    }

    private void makeEntities() {
        userOne = makeUser("user001", "user001@email.com");
        userTwo = makeUser("user002", "user002@email.com");

        requestOne = makeRequest("requestOne description", userTwo, date);
        ItemRequest requestTwo = makeRequest("requestTwo description", userTwo, date);

        itemOne = makeItem("item001", "item001 description", true, userOne, requestOne);
        itemTwo = makeItem("item002", "item002 description", true, userOne, requestOne);
        itemThree = makeItem("item003", "item003 description", false, userTwo, requestTwo);
        testItems = List.of(itemOne, itemTwo, itemThree);
    }
}
