package org.ifmo.ru.lab4back.EJB;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.ifmo.ru.lab4back.entities.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEJBTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private UserEJB userEJB;

    @Test
    void testCreateUser_hashesPasswordAndPersists() {
        UserEntity user = new UserEntity();
        user.setLogin("u1");
        user.setPassword("plainPass");

        userEJB.createUser(user);

        verify(entityManager, times(1)).persist(user);

        assertNotNull(user.getPassword());
        assertNotEquals("plainPass", user.getPassword());
        assertTrue(BCrypt.checkpw("plainPass", user.getPassword()));
    }

    @Test
    void testCheckPassword_correctAndIncorrect() {
        String login = "alice";
        String plain = "secret";
        String hashed = BCrypt.hashpw(plain, BCrypt.gensalt());

        UserEntity stored = new UserEntity();
        stored.setLogin(login);
        stored.setPassword(hashed);

        when(entityManager.find(UserEntity.class, login)).thenReturn(stored);

        assertTrue(userEJB.checkPassword(login, plain));

        assertFalse(userEJB.checkPassword(login, "wrong"));
    }

    @Test
    void testGetAllUsers_and_getAllUsersLogins() {
        UserEntity u1 = new UserEntity();
        u1.setLogin("a");
        UserEntity u2 = new UserEntity();
        u2.setLogin("b");

        List<UserEntity> list = new ArrayList<>();
        list.add(u1);
        list.add(u2);

        @SuppressWarnings("unchecked")
        TypedQuery<UserEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(UserEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(list);

        List<UserEntity> res = userEJB.getAllUsers();
        assertSame(list, res);
        verify(entityManager, times(1)).createQuery(anyString(), eq(UserEntity.class));
        verify(query, times(1)).getResultList();

        List<String> logins = userEJB.getAllUsersLogins();
        assertEquals(2, logins.size());
        assertTrue(logins.contains("a"));
        assertTrue(logins.contains("b"));
    }

    @Test
    void testUpdateUser_mergesAndReturns() {
        UserEntity user = new UserEntity();
        user.setLogin("x");
        user.setPassword("p");

        UserEntity merged = new UserEntity();
        merged.setLogin("x");
        merged.setPassword("p-merged");

        when(entityManager.merge(user)).thenReturn(merged);

        UserEntity result = userEJB.updateUser(user);
        assertSame(merged, result);
        verify(entityManager, times(1)).merge(user);
    }

    @Test
    void testSetUserActive_and_isUserActive() {
        String login = "activeUser";
        UserEntity user = new UserEntity();
        user.setLogin(login);
        user.setActive(false);

        when(entityManager.find(UserEntity.class, login)).thenReturn(user);
        when(entityManager.merge(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        userEJB.setUserActive(login, true);

        assertTrue(user.isActive());
        verify(entityManager, times(1)).merge(user);

        when(entityManager.find(UserEntity.class, login)).thenReturn(user);
        assertTrue(userEJB.isUserActive(login));

        when(entityManager.find(UserEntity.class, "no")).thenReturn(null);
        assertFalse(userEJB.isUserActive("no"));
    }
}
