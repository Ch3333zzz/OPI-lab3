package org.ifmo.ru.lab4back.beans;

import org.ifmo.ru.lab4back.EJB.PointEJB;
import org.ifmo.ru.lab4back.entities.PointEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import jakarta.ws.rs.core.Response;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PointAddPointTest {

    @Mock
    private PointEJB pointEJB;

    @InjectMocks
    private Point pointBean;

    @Test
    void testAddPointSuccessful() {
        PointEntity input = new PointEntity();
        input.setX("0.2");
        input.setY("0.1");
        input.setR("1");
        input.setOwner("user1");

        when(pointEJB.getAllPoints()).thenReturn(Collections.emptyList());

        Response resp = pointBean.addPoint(input);

        assertNotNull(resp);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());

        Object entity = resp.getEntity();
        assertTrue(entity instanceof PointEntity);
        PointEntity respPoint = (PointEntity) entity;

        assertEquals(input.getX(), respPoint.getX());
        assertEquals(input.getY(), respPoint.getY());
        assertEquals(input.getR(), respPoint.getR());
        assertNotNull(respPoint.getHit());
        assertTrue(respPoint.getExecutionTime() >= 0);
        assertNotNull(respPoint.getCurrentTime());

        verify(pointEJB, times(1)).createPoint(any(PointEntity.class));
    }
}
