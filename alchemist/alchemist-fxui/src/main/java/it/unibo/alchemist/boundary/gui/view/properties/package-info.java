/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/**
 * This package contains new {@link javafx.beans.property.Property JavaFX
 * Properties} specifically written to be {@link java.io.Serializable} and to be
 * used as {@link it.unibo.alchemist.boundary.gui.effects.EffectFX Effects}
 * properties.
 * <p>
 * This choice makes the binding process with JavaFX GUI components (like the
 * {@link it.unibo.alchemist.boundary.gui.controller.EffectPropertiesController
 * effect tuner} drawer) much more easy.
 */
package it.unibo.alchemist.boundary.gui.view.properties;
