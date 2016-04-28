package it.unibo.alchemist.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Time;

import org.junit.Test;

/**
 */
public class TestDoubleTime {

    /**
     * 
     */
    @Test
    public void testDoubleTime() {
        final Time t = new DoubleTime(1);
        assertNotNull(t);
        assertEquals(new DoubleTime(1), t);
        assertNotEquals(new DoubleTime(), t);
        assertEquals(new DoubleTime(1).hashCode(), t.hashCode());
    }

}
