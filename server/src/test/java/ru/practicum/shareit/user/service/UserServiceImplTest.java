package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private final EntityManager em;
    private final UserService userService;
    private final UserDto userDtoOne = makeUserDto("user001", "user001@email.com");
    private final UserDto userDtoTwo = makeUserDto("user002", "user002@email.com");

    @Test
    void shouldCreateUserWhenNameAndEmailAreNotNull() {
        userService.createUser(userDtoOne);

        TypedQuery<User> query = em.createQuery("select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDtoOne.getEmail()).getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDtoOne.getName()));
        assertThat(user.getEmail(), equalTo(userDtoOne.getEmail()));
    }

    @Test
    void shouldFindUserByIdWhenIdIsNotNull() {
        User entity = UserMapper.toUser(userDtoOne);
        em.persist(entity);
        em.flush();

        UserDto targetUser = userService.findUserById(entity.getId());

        assertNotNull(targetUser);
        assertThat(targetUser, allOf(hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(userDtoOne.getName())),
                hasProperty("email", equalTo(userDtoOne.getEmail())))
        );
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        Long userId = null;
        String message = "Пользователь с id " + userId + " не найден!";

        Exception exception = assertThrows(ValidationException.class, () -> userService.findUserById(null));

        assertThat(message, equalTo(exception.getMessage()));
    }

    @Test
    void findAllUsers() {
        List<UserDto> sourceUsers = List.of(userDtoOne, userDtoTwo);

        for (UserDto userDto : sourceUsers) {
            User entity = UserMapper.toUser(userDto);
            em.persist(entity);
        }
        em.flush();

        List<UserDto> targetUsers = userService.findAllUsers();

        assertThat(targetUsers, hasSize(sourceUsers.size()));

        for (UserDto sourceUser : sourceUsers) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }
    }

    @Test
    void shouldUpdateUserWhenAllFieldsAreFilled() {
        User userOld = UserMapper.toUser(userDtoOne);
        User entity = em.merge(userOld);
        Long entityId = entity.getId();
        userDtoTwo.setId(entityId);

        userService.updateUser(userDtoTwo);
        TypedQuery<User> query = em.createQuery("select u from User u where u.id = :id", User.class);
        User user = query.setParameter("id", entityId).getSingleResult();

        assertThat(user, allOf(
                hasProperty("id", equalTo(userDtoTwo.getId())),
                hasProperty("name", equalTo(userDtoTwo.getName())),
                hasProperty("email", equalTo(userDtoTwo.getEmail()))
        ));
    }

    @Test
    void shouldHoldUserNameWhenNewNameIsNull() {
        User userOld = UserMapper.toUser(userDtoOne);
        User entity = em.merge(userOld);
        Long entityId = entity.getId();
        userDtoTwo.setId(entityId);
        userDtoTwo.setName(null);

        userService.updateUser(userDtoTwo);
        TypedQuery<User> query = em.createQuery("select u from User u where u.id = :id", User.class);
        User user = query.setParameter("id", entityId).getSingleResult();

        assertThat(user, allOf(
                hasProperty("id", equalTo(userDtoTwo.getId())),
                hasProperty("name", equalTo(userDtoOne.getName())),
                hasProperty("email", equalTo(userDtoTwo.getEmail()))
        ));
    }

    @Test
    void shouldHoldUserEmailWhenNewEmailIsNull() {
        User userOld = UserMapper.toUser(userDtoOne);
        User entity = em.merge(userOld);
        Long entityId = entity.getId();
        userDtoTwo.setId(entityId);
        userDtoTwo.setEmail(null);

        userService.updateUser(userDtoTwo);
        TypedQuery<User> query = em.createQuery("select u from User u where u.id = :id", User.class);
        User user = query.setParameter("id", entityId).getSingleResult();

        assertThat(user, allOf(
                hasProperty("id", equalTo(userDtoTwo.getId())),
                hasProperty("name", equalTo(userDtoTwo.getName())),
                hasProperty("email", equalTo(userDtoOne.getEmail()))
        ));
    }

    @Test
    void shouldHoldUserEmailWhenNewEmailIsBlank() {
        User userOld = UserMapper.toUser(userDtoOne);
        User entity = em.merge(userOld);
        Long entityId = entity.getId();
        userDtoTwo.setId(entityId);
        userDtoTwo.setEmail("");

        userService.updateUser(userDtoTwo);
        TypedQuery<User> query = em.createQuery("select u from User u where u.id = :id", User.class);
        User user = query.setParameter("id", entityId).getSingleResult();

        assertThat(user, allOf(
                hasProperty("id", equalTo(userDtoTwo.getId())),
                hasProperty("name", equalTo(userDtoTwo.getName())),
                hasProperty("email", equalTo(userDtoOne.getEmail()))
        ));
    }

    @Test
    void shouldDeleteUserWhenUserExists() {
        List<UserDto> sourceUsers = List.of(userDtoOne, userDtoTwo);

        for (UserDto userDto : sourceUsers) {
            User entity = UserMapper.toUser(userDto);
            em.persist(entity);
        }
        em.flush();

        TypedQuery<User> queryToGetUser = em.createQuery("select u from User u where u.email = :email", User.class);
        User userToDelete = queryToGetUser.setParameter("email", sourceUsers.get(0).getEmail()).getSingleResult();
        userService.deleteUser(userToDelete.getId());

        TypedQuery<User> queryToGetAllUsers = em.createQuery("select u from User u", User.class);
        List<User> targetUsers = queryToGetAllUsers.getResultList();

        assertThat(targetUsers, hasSize(sourceUsers.size() - 1));
        assertThat(sourceUsers, not(hasItem(allOf(
                hasProperty("name", equalTo(userToDelete.getName())),
                hasProperty("description", equalTo(userToDelete.getEmail()))
        ))));
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        Long userId = 999L;

        Exception exception = assertThrows(EntityNotFoundException.class, () -> userService.findUserById(userId));
        String expectedMessage = "Пользователь с id " + userId + " не найден!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    private UserDto makeUserDto(String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }
}
