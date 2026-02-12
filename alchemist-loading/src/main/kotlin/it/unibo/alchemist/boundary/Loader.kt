/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary

import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Position
import java.io.Serializable

/**
 * An entity which is able to produce an Alchemist [Simulation], resolving user defined variable values.
 */
interface Loader : Serializable {
    /**
     * Allows to access the currently defined constants, namely variables defined in the simulation file whose value is
     * constant and does not depend on the value of any free variable (directly or indirectly).
     *
     * @return a [Map] between variable names and their computed value
     */
    val constants: Map<String, Any?>

    /**
     * @return remote dependencies files
     */
    val remoteDependencies: List<String>

    /**
     * Returns the launcher to be used in to manage the simulation lifecycle.
     *
     * @return launcher
     */
    val launcher: Launcher

    /**
     * Allows to access the currently defined dependent variable (those variables whose value can be determined given a
     * valid set of values for the free variables).
     *
     * @return a [Map] between variable names and their actual
     * representation
     */
    val dependentVariables: Map<String, DependentVariable<*>>

    /**
     * @return a [Map] between variable names and their actual
     * representation
     */
    val variables: Map<String, Variable<*>>

    /**
     * Returns a simulation with all the variables set to their default values.
     *
     * This is equivalent to calling [getWith] with an empty values map.
     *
     * Type parameters:
     * - T: the concentration type used by the simulation
     * - P: the position type used by the simulation
     *
     * @return an instance of [Simulation] with variables initialized to their defaults
     */
    fun <T, P : Position<P>> getDefault(): Simulation<T, P> = getWith(emptyMap<String, Nothing>())

    /**
     * Returns a simulation with variables set according to the provided [values] map.
     *
     * Each entry in [values] maps a variable name to the desired value. Variables not
     * present in the map will be initialized to their default values.
     *
     * Parameters:
     * - values: a map specifying name-value bindings for the variables in this scenario
     *
     * Type parameters:
     * - T: the concentration type used by the simulation
     * - P: the position type used by the simulation
     *
     * @return an instance of [Simulation] with variables initialized to the specified values
     */
    fun <T, P : Position<P>> getWith(values: Map<String, *>): Simulation<T, P>

    /**
     * Launches the simulations as configured by this loader.
     * A custom [launcher] can be provided.
     * Blocking, returns when all simulations are completed.
     * @param launcher the launcher to be used
     */
    fun launch(launcher: Launcher = this.launcher): Unit = launcher.launch(this)
}
