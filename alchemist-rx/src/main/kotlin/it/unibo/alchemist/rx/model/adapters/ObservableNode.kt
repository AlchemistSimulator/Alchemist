/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.adapters

import arrow.core.Option
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.rx.model.Disposable
import it.unibo.alchemist.rx.model.observation.Observable
import it.unibo.alchemist.rx.model.observation.ObservableMutableMap
import it.unibo.alchemist.rx.model.observation.ObservableMutableMap.ObservableMapExtensions.upsertValue

/**
 * A wrapper class for a `Node` that provides observable properties and methods for tracking and reacting to changes
 * in the node's contents. It delegates the underlying node functionality to the original `Node` instance while adding
 * observable-based functionality for reactive programming.
 *
 * @param T The type of the concentrations associated with molecules in the node.
 * @param source The `Node` instance being wrapped to provide observable support.
 */
class ObservableNode<T>(private val source: Node<T>) :
    Node<T> by source,
    Disposable {

    /**
     * This node's contents as an [ObservableMutableMap], meaning that
     * a new value for a given molecule is emitted if the concentration
     * associated with that molecule changes, or [arrow.core.none] if
     * the concentration is removed.
     */
    val observableContents: ObservableMutableMap<Molecule, T> =
        ObservableMutableMap(source.contents.toMutableMap())

    /**
     * An observable that emits the total count of unique molecules currently present in the observable contents.
     */
    val observableMoleculeCount: Observable<Int> = observableContents.map { it.keys.size }

    /**
     * Observes whether a specific molecule is present in this node.
     *
     * @param molecule The molecule to check for presence in the observable contents.
     * @return An observable that emits `true` if the molecule is present, and `false` otherwise.
     */
    fun observeContains(molecule: Molecule): Observable<Boolean> = observableContents.map { it.contains(molecule) }

    /**
     * Observes the concentration value of a specific molecule in the observable map.
     *
     * @param molecule The molecule for which the concentration is to be observed.
     * @return An observable that emits the current concentration of the specified molecule wrapped in an `Option`.
     */
    fun observeConcentration(molecule: Molecule): Observable<Option<T>> = observableContents[molecule]

    /**
     * Updates or inserts the concentration of a molecule in the observable map. If the molecule
     * already exists, its concentration is updated using the provided transformation function.
     * If the molecule does not exist, a new concentration is created using the transformation function.
     *
     * @param molecule The molecule whose concentration is to be updated or inserted.
     * @param concentrationUpdate A function that computes the new concentration based on the current
     * concentration (or `null` if the molecule does not exist).
     */
    fun upsertConcentration(molecule: Molecule, concentrationUpdate: (T?) -> T) {
        observableContents.upsertValue(molecule, concentrationUpdate)
        observableContents[molecule].current.getOrNull()?.let { source.setConcentration(molecule, it) }
    }

    /**
     * Dispose every observable associated with this node's content.
     */
    override fun dispose() {
        observableMoleculeCount.dispose()
        observableContents.dispose()
        // leaking [observeContains]
    }

    override fun removeConcentration(moleculeToRemove: Molecule) {
        source.removeConcentration(moleculeToRemove)
        observableContents.remove(moleculeToRemove)
    }

    override fun setConcentration(molecule: Molecule, concentration: T) {
        source.setConcentration(molecule, concentration)
        observableContents.put(molecule, concentration)
    }

    override fun cloneNode(currentTime: Time): Node<T> = ObservableNode(source.cloneNode(currentTime))

    /**
     * Simple node factory methods.
     */
    companion object NodeExtension {

        /**
         * Converts the current `Node` instance into an `ObservableNode`.
         * If the instance is already an `ObservableNode`, it returns the instance itself.
         * Otherwise, it wraps the instance as a new `ObservableNode`.
         *
         * @return An `ObservableNode` representation of the current `Node` instance.
         */
        fun <T> Node<T>.asObservableNode(): ObservableNode<T> = this as? ObservableNode<T> ?: ObservableNode(this)
    }
}
