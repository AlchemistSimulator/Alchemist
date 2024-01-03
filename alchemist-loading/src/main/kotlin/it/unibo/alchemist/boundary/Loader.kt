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
 * An entity which is able to produce an Alchemist [InitializedEnvironment], resolving user defined variable values.
 */
interface Loader<ProgressMeasure : Comparable<ProgressMeasure>> : Serializable {

    /**
     * Allows to access the currently defined constants, namely variables defined in the simulation file whose value is
     * constant and does not depend on the value of any free variable (directly or indirectly).
     *
     * @return a [Map] between variable names and their computed value
     */
    val constants: Map<String, Any>

    /**
     * @return dependencies files
     */
    val remoteDependencies: List<String>

    /**
     * Returns launcher to be used in the simulation.
     *
     * @return launcher
     */
    val launcher: Launcher<ProgressMeasure>

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
     * @param <T> concentration type
     * @param <P> position type
     * @return an [InitializedEnvironment] with all the variables set at their
     * default values
     </P></T> */
    fun <T, P : Position<P>> getDefault(): Simulation<T, P> {
        return getWith(emptyMap<String, Any>())
    }

    /**
     * @param values a map specifying name-value bindings for the variables in this
     * scenario
     * @param <T>    concentration type
     * @param <P>    position type
     * @return an [InitializedEnvironment] with all the variables set at the
     * specified values. If the value is unspecified, the default is
     * used instead
     </P></T> */
    fun <T, P : Position<P>> getWith(values: Map<String, *>): Simulation<T, P>

    /**
     * Launches the simulations as configured by this loader.
     *
     * @param forceAutostart if true, the simulation will start automatically
     * @param launcher the launcher to be used
     * @return a [Progress] object to monitor the progress of the simulation
     */
    fun launch(
        forceAutostart: Boolean = false,
        launcher: Launcher<ProgressMeasure> = this.launcher,
    ): Progress<ProgressMeasure> = launcher.launch(this, forceAutostart)
}
