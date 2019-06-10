/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.projectview.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import it.unibo.alchemist.boundary.projectview.model.Project;

/**
 * A class with static methods that load or save data in a json file.
 *
 */
public final class ProjectIOUtils {

    private static final String PROJECT_FILE = File.separator + ".alchemist_project_descriptor.json";

    private ProjectIOUtils() {
    }

    /**
     * 
     * @param directory The folder directory.
     * @return Deserializing reader of Json. 
     */
    public static Project loadFrom(final String directory) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Project.class, new InstanceCreator<Project>() {
                    @Override
                    public Project createInstance(final Type type) {
                        return new Project(new File(directory));
                    }
                })
                .setPrettyPrinting()
                .create();
        final String actualPath = directory + PROJECT_FILE;
        if (new File(actualPath).exists() && new File(actualPath).isFile()) {
            try {
                return gson.fromJson(new FileReader(actualPath), Project.class);
                /* TODO:
                 * Create a dry-run
                 * R
                 */
            } catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return null;
        }
    }

    /**
     * 
     * @param project a entity Project.
     * @param directory a folder path.
     */
    public static void saveTo(final Project project, final String directory) {
        if (new File(directory).exists() && new File(directory).isDirectory()) {
            try {
                final Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Files.asCharSink(new File(directory + PROJECT_FILE), StandardCharsets.UTF_8).write(gson.toJson(project));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
