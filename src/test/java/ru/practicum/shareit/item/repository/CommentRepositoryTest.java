package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class CommentRepositoryTest {

    private final LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User owner;
    private User author;
    private Item item;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = createUser("owner", "owner@email.com");
        author = createUser("author", "author@email.com");
        item = createItem();
        comment = createComment();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Test
    void findCommentsByItemId() {
        List<Comment> result = commentRepository.findCommentsByItemId(item.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(comment.getId(), result.get(0).getId());
        assertEquals(comment.getItem().getId(), result.get(0).getItem().getId());
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item createItem() {
        Item item = new Item();
        item.setName("itemOne");
        item.setDescription("itemOne description");
        item.setAvailable(true);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    private Comment createComment() {
        Comment comment = new Comment();
        comment.setText("comment");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(date.minusDays(1L));
        return commentRepository.save(comment);
    }
}
