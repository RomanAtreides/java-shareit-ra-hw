package ru.practicum.shareit.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ItemRequestRepositoryTest {

    private final LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User requester;
    private ItemRequest requestOne;
    private ItemRequest requestTwo;

    @BeforeEach
    void setUp() {
        requester = createUser();
        requestOne = createRequest("requestOne description", requester, date.minusDays(2L));
        requestTwo = createRequest("requestTwo description", requester, date.minusDays(1L));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        itemRequestRepository.deleteAll();
    }

    @Test
    void findItemRequestsByRequester_IdOrderByCreatedDesc() {
        List<ItemRequest> result = itemRequestRepository
                .findItemRequestsByRequester_IdOrderByCreatedDesc(requester.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(requestTwo.getId(), result.get(0).getId());
        assertEquals(requestOne.getId(), result.get(1).getId());
    }

    @Test
    void findItemRequestById() {
        ItemRequest result = itemRequestRepository.findItemRequestById(requestTwo.getId());

        assertNotNull(result);
        assertEquals(requestTwo.getId(), result.getId());
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setName("requester");
        user.setEmail("requester@email.com");
        return userRepository.save(user);
    }

    private ItemRequest createRequest(String description, User user, LocalDateTime date) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequester(user);
        request.setCreated(date);
        return itemRequestRepository.save(request);
    }
}
