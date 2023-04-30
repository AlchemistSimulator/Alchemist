/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.beans.property.Property;

/**
 * This interface lets implement classes for JavaFX custom property
 * serialization.
 * 
 * @param <T>
 *            the {@link Property} type
 */
public interface PropertyTypeAdapter<T extends Property<?>> extends JsonSerializer<T>, JsonDeserializer<T> {
    /** Static default JSON key for field "name". */
    String NAME = "name";
    /** Static default JSON key for field "value". */
    String VALUE = "value";
    /** Static default JSON key for field "bean". */
    String BEAN = "bean";

    @Override 
    T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context);

    @Override 
    JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context);
}
