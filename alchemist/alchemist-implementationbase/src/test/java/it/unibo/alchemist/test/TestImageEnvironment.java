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
import org.kaikikm.threadresloader.ResourceLoader;

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
            assertNotNull(new ImageEnvironment<Object>(ResourceLoader.getResource("piantina1.png").getPath()));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina1.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/planimetriabn1.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/planimetriabn1.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina2.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina2.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina3.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina3.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina4.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina4.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina5.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina5.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina6.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina6.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina7.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina7.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina8.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina8.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina9.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/piantina9.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/Pastorello.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/Pastorello.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/Senzanome.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/Senzanome.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/duelocalioreno-pianta3.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/duelocalioreno-pianta3.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/2rettangolo_nero.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/2rettangolo_nero.png", MAX, MAX, MAX));
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
            assertNotNull(new ImageEnvironment<Object>("/PlanimetriaChiaravalle1.png"));
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
            assertNotNull(new ImageEnvironment<Object>("/PlanimetriaChiaravalle1.png", MAX, MAX, MAX));
        } catch (IOException e) {
            assumeNoException(e);
        }
    }
}
