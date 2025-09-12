package org.ifmo.ru.lab4back.beans;

import org.ifmo.ru.lab4back.EJB.UserEJB;
import org.ifmo.ru.lab4back.entities.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import jakarta.ws.rs.core.Response;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserBeanTest {

    @Mock
    private UserEJB userEJB;

    @InjectMocks
    private User userBean;

    @Test
    void testCheckPassword_correctPassword_setsActiveAndReturnsOk() {
        UserEntity stored = new UserEntity();
        stored.setLogin("alice");
        stored.setPassword("hashed");
        stored.setActive(false);

        when(userEJB.getAllUsersLogins()).thenReturn(List.of("alice"));
        when(userEJB.getUser("alice")).thenReturn(stored);
        when(userEJB.checkPassword("alice", "correct")).thenReturn(true);

        UserEntity request = new UserEntity();
        request.setLogin("alice");
        request.setPassword("correct");

        Response resp = userBean.checkPassword(request);

        assertNotNull(resp);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        verify(userEJB, times(1)).setUserActive("alice", true);
    }

    @Test
    void testCheckPassword_incorrectPassword_returnsConflict() {
        UserEntity stored = new UserEntity();
        stored.setLogin("bob");
        stored.setPassword("hashed");
        stored.setActive(false);

        when(userEJB.getAllUsersLogins()).thenReturn(List.of("bob"));
        when(userEJB.getUser("bob")).thenReturn(stored);
        when(userEJB.checkPassword("bob", "wrong")).thenReturn(false);

        UserEntity request = new UserEntity();
        request.setLogin("bob");
        request.setPassword("wrong");

        Response resp = userBean.checkPassword(request);

        assertNotNull(resp);
        assertEquals(Response.Status.CONFLICT.getStatusCode(), resp.getStatus());
        verify(userEJB, never()).setUserActive(anyString(), anyBoolean());
    }

    @Test
    void testAddUser_whenLoginExists_returnsConflict() {
        UserEntity request = new UserEntity();
        request.setLogin("existing");
        request.setPassword("p");

        when(userEJB.getAllUsersLogins()).thenReturn(List.of("existing"));

        Response resp = userBean.addUser(request);

        assertNotNull(resp);
        assertEquals(Response.Status.CONFLICT.getStatusCode(), resp.getStatus());
        verify(userEJB, never()).createUser(any());
    }

    @Test
    void testAddUser_whenNewUser_createdAndActivated() {
        UserEntity request = new UserEntity();
        request.setLogin("newUser");
        request.setPassword("p");

        when(userEJB.getAllUsersLogins()).thenReturn(List.of());

        Response resp = userBean.addUser(request);

        assertNotNull(resp);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        verify(userEJB, times(1)).createUser(any(UserEntity.class));
        verify(userEJB, times(1)).setUserActive("newUser", true);
    }

    @Test
    void testCheckLogin_existing_returnsOk() {
        when(userEJB.getAllUsersLogins()).thenReturn(List.of("john"));

        Response resp = userBean.checkLogin("john");

        assertNotNull(resp);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
    }

    @Test
    void testCheckLogin_notExisting_returnsNotFound() {
        when(userEJB.getAllUsersLogins()).thenReturn(List.of("someone"));

        Response resp = userBean.checkLogin("unknown");

        assertNotNull(resp);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), resp.getStatus());
    }
}
