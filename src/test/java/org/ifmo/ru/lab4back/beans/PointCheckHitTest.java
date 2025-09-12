package org.ifmo.ru.lab4back.beans;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointCheckHitTest {

    private final Point point = new Point();

    @Test
    void testCheckHit_positiveQuadrant_rectangle() {
        assertEquals("true", point.checkHit("0.2", "0.5", "1"));
        assertEquals("false", point.checkHit("0.6", "0.5", "1"));
    }

    @Test
    void testCheckHit_positiveR_quadrant2_circle() {
        assertEquals("true", point.checkHit("-0.5", "0.5", "1"));
        assertEquals("false", point.checkHit("-0.8", "0.8", "1"));
    }

    @Test
    void testCheckHit_negativeQuadrant_line() {
        assertEquals("true", point.checkHit("-0.2", "-0.1", "1"));
        assertEquals("false", point.checkHit("-1.0", "-1.0", "1"));
    }

    @Test
    void testCheckHit_zeroR() {
        assertEquals("false", point.checkHit("0", "0", "0"));
    }

    @Test
    void testCheckHit_invalidNumber_throws() {
        assertThrows(NumberFormatException.class, () -> point.checkHit("a", "0", "1"));
    }
}