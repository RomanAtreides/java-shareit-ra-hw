package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User requester;
    private ItemRequest request;
    private Item itemOne;
    private Item itemTwo;

    @BeforeEach
    void setUp() {
        owner = createUser("owner", "owner@email.com");
        requester = createUser("requester", "requester@email.com");
        request = createItemRequest();
        itemOne = createItem("Отвертка-01", "Крестовая отвертка", false, owner, request);
        itemTwo = createItem("Дрель-02", "Мощная дрель", true, owner, null);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
    }

    @Test
    void findItemsByOwnerIdOrderByIdAsc() {
        List<Item> result = itemRepository.findItemsByOwnerIdOrderByIdAsc(owner.getId(), Pageable.unpaged());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(result.get(0).getOwner(), result.get(1).getOwner());
        assertEquals(itemOne.getId(), result.get(0).getId());
        assertEquals(itemTwo.getId(), result.get(1).getId());
        assertEquals(itemOne.getName(), result.get(0).getName());
        assertEquals(itemTwo.getName(), result.get(1).getName());
        assertEquals(itemOne.getDescription(), result.get(0).getDescription());
        assertEquals(itemTwo.getDescription(), result.get(1).getDescription());
        assertEquals(itemOne.getOwner(), result.get(0).getOwner());
        assertEquals(itemTwo.getOwner(), result.get(1).getOwner());
    }

    @Test
    void findItemsByRequestId() {
        List<Item> result = itemRepository.findItemsByRequestId(request.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(result.get(0).getId(), itemOne.getId());
        assertEquals(result.get(0).getRequest().getId(), itemOne.getRequest().getId());
    }

    @Test
    void findCountOfUserItems() {
        Integer result = itemRepository.findCountOfUserItems(owner.getId());

        assertNotNull(result);
        assertEquals(2, result);
    }

    @Test
    void findItemsByNameOrDescription() {
        String word = "дрель";
        List<Item> result = itemRepository.findItemsByNameOrDescription(word, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemTwo.getId(), result.get(0).getId());
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item createItem(String name, String description, boolean available, User owner, ItemRequest request) {
        Item item = Item.builder()
                .name(name)
                .description(description)
                .available(available)
                .owner(owner)
                .request(request)
                .build();
        return itemRepository.save(item);
    }

    private ItemRequest createItemRequest() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("request description");
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusDays(1L));
        return itemRequestRepository.save(itemRequest);
    }
}
