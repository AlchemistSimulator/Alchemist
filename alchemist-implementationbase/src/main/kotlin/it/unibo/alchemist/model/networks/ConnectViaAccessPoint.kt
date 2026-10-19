/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.observables.util.ObservableSets.combineLatest
import it.unibo.alchemist.model.observation.Observable

/**
 * Network model that connects each node to its closest access point within range, and to the other nodes within
 * range served by the same access point; access points are connected to each other when within range.
 * The closest access point is the nearest one, ties being broken by the lowest node id.
 *
 * @param T the concentration type
 * @param P the position type
 * @param environment the environment whose nodes this model links
 * @param radius the maximum connection radius
 * @param accessPointId the molecule id used to mark access-point nodes
 */
class ConnectViaAccessPoint<T, P : Position<P>>(
    environment: Environment<T, P>,
    radius: Double,
    accessPointId: Molecule,
) : AccessPointNetworkModel<T, P, ConnectViaAccessPoint.Role<T>>(environment, radius, accessPointId) {

    override fun isLinked(center: Node<T>, centerInput: Role<T>, neighbor: Node<T>, neighborInput: Role<T>): Boolean =
        when {
            centerInput.isAccessPoint -> neighborInput.isAccessPoint || neighborInput.closestAccessPoint == center
            else -> centerInput.closestAccessPoint?.let { servedBy ->
                neighbor == servedBy || !neighborInput.isAccessPoint && neighborInput.closestAccessPoint == servedBy
            } ?: false
        }

    /*
     * The closest access point of a node depends on the nodes within its range, on whether they are access points, and
     * on all their positions.
     */
    override fun observeInput(node: Node<T>): Observable<Role<T>> =
        node.containsMolecule(accessPointId).mergeWith(observeClosestAccessPoint(node), ::Role)

    private fun closestAccessPoint(node: Node<T>): Node<T>? = environment.nodesAround(node, range)
        .asSequence()
        .filter { it.isAccessPoint }
        .minWithOrNull(compareBy({ environment.distanceBetween(node, it) }, { it.id }))

    private fun observeClosestAccessPoint(node: Node<T>): Observable<Node<T>?> =
        environment.observeNodesAround(node, range)
            .combineLatest({ member ->
                member.containsMolecule(accessPointId).mergeWith(environment.positionOf(member)) { isAccessPoint, at ->
                    Triple(member, isAccessPoint, at)
                }
            }) { it.toList() }
            .mergeWith(environment.positionOf(node)) { _, _ -> closestAccessPoint(node) }

    /**
     * The role of a node: whether it [isAccessPoint], and its [closestAccessPoint] within range, if any.
     */
    data class Role<T>(val isAccessPoint: Boolean, val closestAccessPoint: Node<T>?)
}
