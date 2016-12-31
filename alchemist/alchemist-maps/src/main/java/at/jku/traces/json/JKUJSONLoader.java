/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package at.jku.traces.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.danilopianini.io.FileUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.gson.Gson;

import it.unibo.alchemist.model.interfaces.IGPSTrace;

/**
 */
public final class JKUJSONLoader implements Serializable {

    private static final long serialVersionUID = 7144531714361675479L;
    private static final Gson GSON = new Gson();
    private static final Logger L = LoggerFactory.getLogger(JKUJSONLoader.class);

    /**
     * @param f
     *            the file
     * @param c
     *            the class to load
     * @param <C>
     *            the type of the objects
     * @return a list of {@link Object}s of class c.
     * @throws IOException
     *             if there is an I/O error
     */
    public static <C> List<C> loadJsonObjects(final File f, final Class<C> c) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), Charsets.UTF_8))) {
            final List<C> res = new ArrayList<>();
            while (in.ready()) {
                final C el = GSON.fromJson(in.readLine(), c);
                res.add(el);
            }
            return res;
        }
    }

    /**
     * @param args
     *            the first argument must be the input file path, the second
     *            argument must be the output
     * @throws IOException
     *             if there is an I/O error
     */
    public static void main(final String[] args) throws IOException {
        if (args.length != 2) {
            L.error("Usage: java " + JKUJSONLoader.class.getCanonicalName() + " source dest");
            System.exit(1);
        }
        final String source = args[0];
        final String dest = args[1];
        final List<? extends IGPSTrace> l = loadJsonObjects(new File(source), UserTrace.class);
        double mintime = Double.MAX_VALUE;
        for (int i = 0; i < l.size(); i++) {
            final IGPSTrace p = l.get(i);
            p.setId(i);
            final double time = p.getStartTime();
            if (time < mintime) {
                mintime = time;
            }
        }
        for (int i = 0; i < l.size(); i++) {
            final IGPSTrace p = l.get(i);
            p.normalizeTimes(mintime);
        }
        FileUtilities.objectToFile((Serializable) l, dest, false);
    }

    /**
     * 
     */
    private JKUJSONLoader() {
    }

}
