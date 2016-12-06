package it.unibo.alchemist.boundary.projectview;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the creation of new classpath.
 */
public class TestClassLoaderChange {

    private static final Logger L = LoggerFactory.getLogger(TestClassLoaderChange.class);

    /**
     * 
     *
     */
    public static class Printer implements Runnable {
        @Override
        public void run() {
            L.info(Arrays.toString(((URLClassLoader) Printer.class.getClassLoader()).getURLs()));
        }
    }

    /**
     * 
     * @throws MalformedURLException Error during the building of an URL.
     * @throws ClassNotFoundException Error when the class was not found.
     * @throws InstantiationException Error during the instantiation of class.
     * @throws IllegalAccessException Error if the class is not accessible.
     */
    @Test
    public void testChangeClassLoader() throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        final URLClassLoader current = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        final URL myNewURL = Paths.get("myTest").toUri().toURL();
        final URL[] urls = ArrayUtils.addAll(new URL[]{myNewURL}, current.getURLs());
        final URLClassLoader newClassLoader = URLClassLoader.newInstance(urls);
        L.info(Arrays.toString(newClassLoader.getURLs()));
        Assert.assertEquals(myNewURL, newClassLoader.getURLs()[0]);
    }

}
