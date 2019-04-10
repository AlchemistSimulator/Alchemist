/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.projectview.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 *  Singleton that maintains a collection of URL.
 *
 */
public final class URLManager {

    private static URLManager instance = new URLManager();

    private final  List<URL> cp = new LinkedList<>();

    private URLManager() {
    }

    /**
     * 
     * @return instance
     */
    public static URLManager getInstance() {
        return instance;
    }

    /**
     * 
     * @param url URL to be added to the collection
     */
    public void addURL(final URL url) {
        this.cp.add(url);
    }

    /**
     * 
     * @param url URL to be removed from the collection
     */
    public void removeURL(final URL url) {
        this.cp.remove(url);
    }

    /**
     * 
     * @return Unmodifiable copy of current URL collection as list
     */
    public List<URL> getCurrentClasspathSettings() {
        return ImmutableList.copyOf(this.cp);
    }

    /**
     * 
     * @param t Thread
     */
    public void setupThreadClassLoader(final Thread t) {
        t.setContextClassLoader(new URLClassLoader(this.cp.toArray(new URL[this.cp.size()])));
    }
}
