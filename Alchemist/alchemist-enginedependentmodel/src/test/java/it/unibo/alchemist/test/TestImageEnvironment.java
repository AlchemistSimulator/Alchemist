/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import java.io.IOException;

import org.junit.Test;

import it.unibo.alchemist.model.implementations.environments.ImageEnvironment;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;

/**
 * In this JUnit test Class you can control the parsing of some .png image to an
 * environment.
 * 
 */
public class TestImageEnvironment {

    private static final int MAX = 255;

    /**
     * Test the parsing of the image named piantina 1 where black is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina1() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina1.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 1 where white is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina1white() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina1.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named planimetirabn 1 where black is the
     * color of the obstacle..
     */
    @Test
    public void testPlanimetria() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/planimetriabn1.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named planimetirabn 1 where white is the
     * color of the obstacle..
     */
    @Test
    public void testPlanimetriaWhite() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/planimetriabn1.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 2 where black is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina2() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina2.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 2 where white is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina2White() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina2.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 3 where black is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina3() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina3.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 3 white is the color of the
     * obstacle.
     */
    @Test
    public void testPiantina3White() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina3.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 4 where black is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina4() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina4.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 4 where white is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina4White() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina4.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 5 where black is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina5() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina5.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 5 where white is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina5White() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina5.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 6 where black is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina6() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina6.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 6 where white is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina6White() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina6.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 7 where black is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina7() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina7.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 7 where white is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina7White() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina7.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 8 where black is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina8() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina8.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 8 where white is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina8White() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina8.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 9 where black is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina9() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina9.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named piantina 9 where white is the color
     * of the obstacle.
     */
    @Test
    public void testPiantina9White() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/piantina9.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
            fail();
        }
    }

    /**
     * Test the parsing of the image named Pastorello where black is the color
     * of the obstacle.
     */
    @Test
    public void testPastorello() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/Pastorello.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    /**
     * Test the parsing of the image named Pastorello where white is the color
     * of the obstacle.
     */
    @Test
    public void testPastorelloWhite() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/Pastorello.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    /**
     * Test the parsing of the image named Senzanome where black is the color of
     * the obstacle.
     */
    @Test
    public void testSenzanome() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/Senzanome.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    /**
     * Test the parsing of the image named Senzanome where white is the color of
     * the obstacle.
     */
    @Test
    public void testSenzanomeWhite() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/Senzanome.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    /**
     * Test the parsing of the image named duelocalioreno-pianta3 where black is
     * the color of the obstacle.
     */
    @Test
    public void testDuelocali() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/duelocalioreno-pianta3.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    /**
     * Test the parsing of the image named duelocalioreno-pianta3 where white is
     * the color of the obstacle.
     */
    @Test
    public void testDuelocaliWhite() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/duelocalioreno-pianta3.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    /**
     * Test the parsing of the image named 2rettangolo_nero where black is the
     * color of the obstacle.
     */
    @Test
    public void test2Rettangoli() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/2rettangolo_nero.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    /**
     * Test the parsing of the image named 2rettangolo_nero where white is the
     * color of the obstacle.
     */
    @Test
    public void test2RettangoliWhite() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/2rettangolo_nero.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    /**
     * Test the parsing of the image named PlanimetriaChiaravalle1 where black
     * is the color of the obstacle.
     */
    @Test
    public void testPlanimetriaChiaravalle1() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/PlanimetriaChiaravalle1.png").getPath()));
        } catch (IOException e) {
            assumeNoException(e);
        }
    }

    /**
     * Test the parsing of the image named PlanimetriaChiaravalle1 where white
     * is the color of the obstacle.
     */
    @Test
    public void testPlanimetriaChiaravalle1White() {
        try {
            assertNotNull(new ImageEnvironment<Object>(TestImageEnvironment.class.getResource("/PlanimetriaChiaravalle1.png").getPath(), MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
        }
    }
}
