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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.danilopianini.io.FileUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import it.unibo.alchemist.model.interfaces.GPSPoint;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 */
public final class JKUJSONLoader implements Serializable {

    private static final long serialVersionUID = 7144531714361675479L;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(GPSPoint.class, new JsonDeserializer<GPSPoint>() {
                @Override
                public GPSPoint deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
                    if (json.isJsonObject()) {
                        final JsonObject obj = json.getAsJsonObject();
                        if (obj.get("la") != null && obj.get("lo") != null
                                && obj.get("t") != null && obj.get("la").isJsonPrimitive()
                                && obj.get("lo").isJsonPrimitive() && obj.get("t").isJsonPrimitive()) {
                            final double latitude = obj.get("la").getAsDouble();
                            final double longitude = obj.get("lo").getAsDouble();
                            final double time = obj.get("t").getAsDouble();
                            return new GPSPointImpl(latitude, longitude, time);
                        } else {
                            throw new IllegalStateException("An invalid JSON has been provided: unable to get fields from JSON file");
                        }
                    } else {
                        throw new IllegalStateException("An invalid JSON has been provided: unable to get the JSON object to deserialize");
                    }
                }
            })
            .create();
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
        final List<? extends GPSTrace> l = loadJsonObjects(new File(source), UserTrace.class);
        double mintime = Double.MAX_VALUE;
        for (int i = 0; i < l.size(); i++) {
            final GPSTrace p = l.get(i);
            p.setId(i);
            final double time = p.getStartTime();
            if (time < mintime) {
                mintime = time;
            }
        }
        for (int i = 0; i < l.size(); i++) {
            final GPSTrace p = l.get(i);
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
