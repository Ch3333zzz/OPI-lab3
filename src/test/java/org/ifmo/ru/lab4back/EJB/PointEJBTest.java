package org.ifmo.ru.lab4back.EJB;

import org.ifmo.ru.lab4back.entities.PointEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PointEJBTest {

    @Test
    void testGetPointsByOwner_filtersCorrectly() {
        PointEJB pointEJB = new PointEJB();
        PointEJB spy = Mockito.spy(pointEJB);

        PointEntity p1 = new PointEntity();
        p1.setOwner("Arseniy");
        p1.setX("0.1");

        PointEntity p2 = new PointEntity();
        p2.setOwner("Koz");
        p2.setX("0.2");

        PointEntity p3 = new PointEntity();
        p3.setOwner("Arseniy");
        p3.setX("0.3");

        List<PointEntity> all = Arrays.asList(p1, p2, p3);

        Mockito.doReturn(all).when(spy).getAllPoints();

        List<PointEntity> res = spy.getPointsByOwner("Arseniy");
        assertNotNull(res);
        assertEquals(2, res.size());
        assertTrue(res.stream().allMatch(p -> "Arseniy".equals(p.getOwner())));
    }
}
