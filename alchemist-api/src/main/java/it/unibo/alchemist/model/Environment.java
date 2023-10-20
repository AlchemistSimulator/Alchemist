/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import it.unibo.alchemist.core.Simulation;
import org.danilopianini.util.ListSet;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Interface for an environment. Every environment must implement this
 * specification.
 *
 * @param <P> Concentration type
 * @param <T> Position type
 */
public interface Environment<T, P extends Position<? extends P>> extends Serializable, Iterable<Node<T>> {

    /**
     * Add a {@link Layer} to the {@link Environment}.
     *
     * @param m the {@link Molecule} of the {@link Layer}
     * @param l the {@link Layer}
     */
    void addLayer(Molecule m, Layer<T, P> l);

    /**
     * Add a {@link GlobalReaction} to the {@link Environment}.
     *
     * @param reaction the {@link GlobalReaction} to add.
     */
    void addGlobalReaction(GlobalReaction<T> reaction);

    /**
     * Remove a {@link GlobalReaction} from the {@link Environment}.
     *
     * @param reaction {@link GlobalReaction} to remove.
     */
    void removeGlobalReaction(GlobalReaction<T> reaction);

    /**
     * Get the {@link Environment}'s {@link GlobalReaction}s.
     *
     * @return the list of {@link GlobalReaction}s in this {@link Environment}.
     */
    ListSet<GlobalReaction<T>> getGlobalReactions();

    /**
     * This method allows to add a new node to this environment. The environment
     * is responsible to call the right method of the simulation in order to
     * ensure that the reaction is properly scheduled.
     *
     * @param node The node to add
     * @param p    The position where to place it
     * @return true if node is added in the environment
     */
    boolean addNode(Node<T> node, P p);

    /**
     * @param terminator a {@link Predicate} indicating whether the simulation should
     *                   be considered finished
     */
    void addTerminator(Predicate<Environment<T, P>> terminator);

    /**
     * The number of dimensions of this environment.
     *
     * @return the number of dimensions of this environment
     */
    int getDimensions();

    /**
     * Measures the distance between two nodes in the environment.
     *
     * @param n1 the first node
     * @param n2 the second node
     * @return the distance between the two nodes
     */
    double getDistanceBetweenNodes(Node<T> n1, Node<T> n2);

    /**
     * @return the {@link Incarnation} used to initialize the entities of this {@link Environment}, if it has been set.
     */
    @Nonnull
    Incarnation<T, P> getIncarnation();

    /**
     * Get the layer associate to the given molecule. If no Layer is associated
     * with the given molecule, return an empty optional.
     *
     * @param m the {@link Molecule}
     * @return the {@link Optional} containing the {@link Layer} associated with
     * the requested molecule
     */
    Optional<Layer<T, P>> getLayer(Molecule m);

    /**
     * Return all the Layers in this {@link Environment}.
     *
     * @return a {@link ListSet} of {@link Layer}.
     */
    ListSet<Layer<T, P>> getLayers();

    /**
     * @return the current linking rule
     */
    LinkingRule<T, P> getLinkingRule();

    /**
     * @param rule the rule to set
     */
    void setLinkingRule(LinkingRule<T, P> rule);

    /**
     * Given a node, this method returns its neighborhood.
     *
     * @param center The node you want the neighbors to be found
     * @return the neighborhood
     */
    Neighborhood<T> getNeighborhood(Node<T> center);

    /**
     * Allows to access a node known its id. Depending on the implementation, this method may or not be optimized
     * (namely, id could run in constant or linear time with the number of nodes).
     *
     * @param id the node's ID
     * @return the node with that id, or null if it does not exist in this
     * environment
     */
    Node<T> getNodeByID(int id);

    /**
     * All the nodes that exist in current environment.
     *
     * @return All the nodes that exist in current environment
     */
    ListSet<Node<T>> getNodes();

    /**
     * @return the number of nodes currently in the environment
     */
    int getNodeCount();

    /**
     * Given a node (center) this method returns a list of all the surroundings
     * nodes within the given range. Note that this method (depending on the
     * implementation) might be not optimized and it's consequently <b>much</b>
     * better to use {@link Environment#getNeighborhood(Node)} and filter the
     * neighborhood if you are sure that all the nodes within the range are
     * connected to the center.
     *
     * @param center the node to consider as center
     * @param range  the exploration range
     * @return the list of nodes within the range
     */
    ListSet<Node<T>> getNodesWithinRange(Node<T> center, double range);

    /**
     * Given a {@link Position}(center) this method returns a list of all the
     * surroundings nodes within the given range. Note that this method
     * (depending on the implementation) might be not optimized.
     *
     * @param center the {@link Position} to consider as center
     * @param range  the exploration range
     * @return the list of nodes within the range
     */
    ListSet<Node<T>> getNodesWithinRange(P center, double range);

    /**
     * This method allows to know which are the smallest coordinates
     * represented.
     *
     * @return an array of length getDimensions() containing the smallest
     * coordinates for each dimension.
     */
    double[] getOffset();

    /**
     * Calculates the position of a node.
     *
     * @param node the node you want to know the position
     * @return The position
     */
    @Nonnull
    P getPosition(Node<T> node);

    /**
     * @return the current simulation, if present, or throws an
     * {@link IllegalStateException} otherwise
     */
    Simulation<T, P> getSimulation();

    /**
     * @param s the simulation
     */
    void setSimulation(Simulation<T, P> s);

    /**
     * This method returns the size of the environment as an array of length
     * {@link #getDimensions()}. This method must return distance measured with
     * the same unit used by the positions. No non-euclidean distance metrics
     * are allowed.
     *
     * @return the size of this environment
     */
    double[] getSize();

    /**
     * This method returns the size of the environment as an array of length
     * {@link #getDimensions()}. This method must return distance measured with
     * the same unit used for measuring distances. It may or may not return the
     * same result of {@link #getSize()}
     *
     * @return the size of this environment
     */
    double[] getSizeInDistanceUnits();

    /**
     * @return true if all the terminators are true
     */
    boolean isTerminated();

    /**
     * @param coordinates the coordinates of the point
     * @return a {@link Position} compatible with this environment
     */
    P makePosition(Number... coordinates);

    /**
     * This method moves a node in the environment to some position. If node
     * move is unsupported, it does nothing.
     *
     * @param node     The node to move
     * @param position The absolute position in which this node will be moved.
     */
    @Nonnull
    void moveNodeToPosition(@Nonnull Node<T> node, @Nonnull P position);

    /**
     * This method allows to remove a node. If node removal is unsupported, it
     * does nothing.
     *
     * @param node the node to remove
     */
    void removeNode(Node<T> node);

}
