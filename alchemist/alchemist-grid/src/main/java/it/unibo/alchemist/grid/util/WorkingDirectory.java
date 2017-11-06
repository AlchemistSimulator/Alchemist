package it.unibo.alchemist.grid.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;

/**
 * Class that manage a temp local working directory.
 *
 */
public class WorkingDirectory implements AutoCloseable {

    private final File directory;

    /**
     * Create new local temp working directory.
     */
    public WorkingDirectory() {
        this.directory = Files.createTempDir();
    }

    /**
     * Add temp directory to classpath.
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @throws ReflectiveOperationException 
     * @throws IllegalArgumentException 
     * @throws InvocationTargetException 
     * @throws MalformedURLException 
     */
    public void addToClasspath() throws NoSuchMethodException, SecurityException, ReflectiveOperationException, IllegalArgumentException, InvocationTargetException, MalformedURLException {
        final URI u = directory.toURI();
        final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Class<URLClassLoader> urlClass = URLClassLoader.class;
        final Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(urlClassLoader, new Object[]{u.toURL()});
    }

    /**
     * Get folder's file content.
     * @param filename File's name
     * @return File's content
     * @throws IOException 
     */
    public String getFileContent(final String filename) throws IOException {
        final File f = new File(this.getFileAbsolutePath(filename));
        return FileUtils.readFileToString(f, StandardCharsets.UTF_8);
    }

    /**
     * Write multiple files inside the directory.
     * @param files A map with relative paths + files names as kay and files content as value.
     * @throws IOException 
     */
    public void writeFiles(final Map<String, String> files) throws IOException {
        for (final Entry<String, String> e : files.entrySet()) {
            final File f = new File(this.directory.getAbsolutePath() + File.separator + e.getKey());
            f.getParentFile().mkdirs();
            FileUtils.writeStringToFile(f, e.getValue(), StandardCharsets.UTF_8);
        }
    }

    /**
     * 
     * @param filename File name
     * @return Absolute path for given filename in this directory, even if it doesn't exists
     */
    public String getFileAbsolutePath(final String filename) {
        return this.directory.getAbsolutePath() + File.separator + filename;
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(this.directory);
    }

}
