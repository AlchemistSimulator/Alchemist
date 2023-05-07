/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.maps;

import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Route;

/**
 * 
 * Route with total trip time to cross it.
 * 
 * @param <P> type of position in the route
 */
public interface TimedRoute<P extends Position<?>> extends Route<P> {

    /**
     * 
     * @return the total trip time 
     */
    double getTripTime();

}
