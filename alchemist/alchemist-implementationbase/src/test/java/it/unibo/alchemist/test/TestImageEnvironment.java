/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test;

import java.io.IOException;

import org.junit.Test;
import org.kaikikm.threadresloader.ResourceLoader;

import it.unibo.alchemist.model.implementations.environments.ImageEnvironment;
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
            new ImageEnvironment<>(ResourceLoader.getResource("piantina1.png").getPath());
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
            new ImageEnvironment<>("piantina1.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("planimetriabn1.png");
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
            new ImageEnvironment<>("planimetriabn1.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("piantina2.png");
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
            new ImageEnvironment<>("piantina2.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("piantina3.png");
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
            new ImageEnvironment<>("piantina3.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("piantina4.png");
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
            new ImageEnvironment<>("piantina4.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("piantina5.png");
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
            new ImageEnvironment<>("piantina5.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("piantina6.png");
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
            new ImageEnvironment<>("piantina6.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("piantina7.png");
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
            new ImageEnvironment<>("piantina7.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("piantina8.png");
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
            new ImageEnvironment<>("piantina8.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("piantina9.png");
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
            new ImageEnvironment<>("piantina9.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("Pastorello.png");
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
            new ImageEnvironment<>("Pastorello.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("Senzanome.png");
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
            new ImageEnvironment<>("Senzanome.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("duelocalioreno-pianta3.png");
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
            new ImageEnvironment<>("duelocalioreno-pianta3.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("2rettangolo_nero.png");
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
            new ImageEnvironment<>("2rettangolo_nero.png", MAX, MAX, MAX);
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
            new ImageEnvironment<>("PlanimetriaChiaravalle1.png");
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
            new ImageEnvironment<>("PlanimetriaChiaravalle1.png", MAX, MAX, MAX);
        } catch (IOException e) {
            assumeNoException(e);
        }
    }
}
