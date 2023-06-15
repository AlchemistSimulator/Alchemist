/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core.interfaces;

import it.unibo.alchemist.model.interfaces.Actionable;

import java.util.List;

public interface BatchedScheduler<T> extends Scheduler<T> {

    List<Actionable<T>> getNextBatch();

}
