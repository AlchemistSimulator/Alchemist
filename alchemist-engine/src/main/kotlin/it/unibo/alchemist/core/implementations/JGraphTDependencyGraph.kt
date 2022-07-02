/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core.implementations

import it.unibo.alchemist.core.interfaces.DependencyGraph
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import java.lang.IllegalArgumentException
import org.danilopianini.util.ArrayListSet
import org.danilopianini.util.ListSet
import org.danilopianini.util.ListSets
import org.jgrapht.graph.DefaultDirectedGraph

private typealias Edge<T> = Pair<Reaction<T>, Reaction<T>>

/**
 * This class offers an implementation of a dependency graph, namely a
 * data structure which can address in an efficient way the problem of
 * finding those reactions affected by the execution of another
 * reaction. This class relies heavily on the ReactionHandler
 * interface.
 *
 * @param <T> concentration type
 */
class JGraphTDependencyGraph<T>(private val environment: Environment<T, *>) : DependencyGraph<T> {
    private val inGlobals = ArrayListSet<Reaction<T>>()
    private val outGlobals = ArrayListSet<Reaction<T>>()
    private val graph: DefaultDirectedGraph<Reaction<T>, Edge<T>> = DefaultDirectedGraph(null, null, false)

    override fun createDependencies(newReaction: Reaction<T>) {
        val allReactions = graph.vertexSet()
        val neighborhood by lazy { newReaction.node.neighborhood }
        val localReactions by lazy {
            newReaction.node.reactions.filter { allReactions.contains(it) }.asSequence()
        }
        val neighborhoodReactions by lazy {
            neighborhood.asSequence()
                .flatMap { it.reactions.asSequence() }
                .filter { allReactions.contains(it) }
                .toList().asSequence()
        }
        val extendedNeighborhoodReactions by lazy {
            neighborhood.asSequence()
                // Neighbors of neighbors
                .flatMap { it.neighborhood.asSequence() }
                // No duplicates
                .distinct()
                // Exclude direct neighbors
                .filterNot { neighborhood.contains(it) }
                .flatMap { it.reactions.asSequence() }
                .filter { allReactions.contains(it) }
                .toList().asSequence()
        }
        val inboundCandidates: Sequence<Reaction<T>> = outGlobals.asSequence() +
            when (newReaction.inputContext) {
                Context.LOCAL ->
                    localReactions + neighborhoodReactions.filter { it.outputContext == Context.NEIGHBORHOOD }
                Context.NEIGHBORHOOD ->
                    localReactions + neighborhoodReactions +
                        extendedNeighborhoodReactions.filter { it.outputContext == Context.NEIGHBORHOOD }
                else -> allReactions.asSequence()
            }
        val outboundCandidates: Sequence<Reaction<T>> = inGlobals.asSequence() +
            when (newReaction.outputContext) {
                Context.LOCAL ->
                    localReactions + neighborhoodReactions.filter { it.inputContext == Context.NEIGHBORHOOD }
                Context.NEIGHBORHOOD ->
                    localReactions + neighborhoodReactions +
                        extendedNeighborhoodReactions.filter { it.inputContext == Context.NEIGHBORHOOD }
                else -> allReactions.asSequence()
            }
        if (!graph.addVertex(newReaction)) {
            throw IllegalArgumentException("$newReaction was already in the dependency graph")
        }
        inboundCandidates.filter { newReaction.dependsOn(it) }
            .forEach { graph.addEdge(it, newReaction, Edge(it, newReaction)) }
        outboundCandidates.filter { it.dependsOn(newReaction) }
            .forEach { graph.addEdge(newReaction, it, Edge(newReaction, it)) }
        if (newReaction.inputContext == Context.GLOBAL) {
            inGlobals.add(newReaction)
        }
        if (newReaction.outputContext == Context.GLOBAL) {
            outGlobals.add(newReaction)
        }
    }

    private val Node<T>.neighborhood
        get() = environment.getNeighborhood(this).neighbors

    private fun Reaction<T>.dependsOn(other: Reaction<T>) =
        inboundDependencies.any { inbound ->
            other.outboundDependencies.any { outbound ->
                inbound.dependsOn(outbound) || outbound.makesDependent(inbound)
            }
        }

    override fun removeDependencies(r: Reaction<T>) {
        if (!graph.removeVertex(r)) {
            throw IllegalStateException("Inconsistent state: $r was not in the reaction pool.")
        }
        if (r.inputContext == Context.GLOBAL && !inGlobals.remove(r)) {
            throw IllegalStateException(
                "Inconsistent state: " + r + " , with global input context, " +
                    "was not in the appropriate reaction pool."
            )
        }
        if (r.inputContext == Context.GLOBAL && !outGlobals.remove(r)) {
            throw IllegalStateException(
                "Inconsistent state: " + r + " , with global output context, " +
                    "was not in the appropriate reaction pool."
            )
        }
    }

    private fun addNeighborDirected(n1: Node<T>, n2: Node<T>) {
        val n2NonGlobalReactions: Iterable<Reaction<T>> by lazy {
            n2.reactions.filterNot { it.outputContext == Context.GLOBAL }
        }
        val n2NeighborhoodReactions: Iterable<Reaction<T>> by lazy {
            n2NonGlobalReactions.filter { it.outputContext == Context.NEIGHBORHOOD }
        }
        val neighborInputInfluencers: Iterable<Reaction<T>> by lazy {
            // All the non-global reactions of the new neighbor
            n2NonGlobalReactions +
                // Plus all the reactions of the new neighbor's neighbors with neighborhood output
                (n2.neighborhood - n1.neighborhood).asSequence()
                    .flatMap { it.reactions.asSequence() }
                    .filter { it.outputContext == Context.NEIGHBORHOOD }
        }
        n1.reactions.forEach { reaction ->
            when (reaction.inputContext) {
                // Local-reading reactions can be only influenced by the new neighbor's neighborhood reactions
                Context.LOCAL -> n2NeighborhoodReactions
                Context.NEIGHBORHOOD -> neighborInputInfluencers
                else -> emptyList()
            }.asSequence()
                .filter { reaction.dependsOn(it) }
                .forEach { graph.addEdge(it, reaction, Edge(it, reaction)) }
        }
    }

    /** @see [DependencyGraph.addNeighbor] */
    override fun addNeighbor(n1: Node<T>, n2: Node<T>) {
        addNeighborDirected(n1, n2)
        addNeighborDirected(n2, n1)
    }

    private fun removeNeighborDirected(n1: Node<T>, n2: Node<T>) {
        val n2NonGlobalReactions by lazy { n2.reactions.filterNot { it.outputContext == Context.GLOBAL } }
        val n2NeighborhoodReactions by lazy { n2NonGlobalReactions.filter { it.outputContext == Context.NEIGHBORHOOD } }
        val neighborInputInfluencers by lazy {
            // All the non-global reactions of the old neighbor
            n2NonGlobalReactions +
                // Plus all the reactions of the new neighbor's neighbors with neighborhood output
                (n2.neighborhood - n1.neighborhood - n1.neighborhood.flatMap { it.neighborhood }).asSequence()
                    .flatMap { it.reactions.asSequence() }
                    .filter { it.outputContext == Context.NEIGHBORHOOD }
                    .toList()
        }
        n1.reactions.forEach { reaction ->
            when (reaction.inputContext) {
                // Local-reading reactions can be only influenced by the new neighbor's neighborhood reactions
                Context.LOCAL -> n2NeighborhoodReactions
                Context.NEIGHBORHOOD -> neighborInputInfluencers
                else -> emptyList()
            }.asSequence()
                .filter { reaction.dependsOn(it) }
                .forEach { graph.removeEdge(it, reaction) }
        }
    }

    override fun removeNeighbor(n1: Node<T>, n2: Node<T>) {
        removeNeighborDirected(n1, n2)
        removeNeighborDirected(n2, n1)
    }

    override fun outboundDependencies(reaction: Reaction<T>) =
        if (graph.containsVertex(reaction)) {
            graph.outgoingEdgesOf(reaction).let { edges ->
                edges.asSequence().map { it.second }.toCollection(ArrayListSet(edges.size))
            }
        } else {
            ListSet.of()
        }

    override fun toString(): String {
        return graph.toString()
    }

    override fun globalInputContextReactions(): ListSet<Reaction<T>> = ListSets.unmodifiableListSet(inGlobals)
}
