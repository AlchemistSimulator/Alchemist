/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * The type which describes the concentration of a molecule
 * 
 * Interface for an environment. Every environment must implement this
 * specification.
 * 
 * @param <T>
 */
public interface Environment<T> extends Serializable, Iterable<Node<T>> {

    /**
     * This method allows to add a new node to this environment. The environment
     * is responsible to call the right method of the simulation in order to
     * ensure that the reaction is properly scheduled.
     * 
     * @param node
     *            The node to add
     * @param p
     *            The position where to place it
     */
    void addNode(Node<T> node, Position p);

    /**
     * The number of dimensions of this environment.
     * 
     * @return the number of dimensions of this environment
     */
    int getDimensions();

    /**
     * Measures the distance between two nodes in the environment.
     * 
     * @param n1
     *            the first node
     * @param n2
     *            the second node
     * @return the distance between the two nodes
     */
    double getDistanceBetweenNodes(Node<T> n1, Node<T> n2);

    /**
     * Given a node, this method returns its neighborhood.
     * 
     * @param center
     *            The node you want the neighbors to be found
     * @return the neighborhood
     */
    Neighborhood<T> getNeighborhood(Node<T> center);

    /**
     * Allows to access a node known its id.
     * 
     * @param id
     *            the node's ID
     * @return the node with that id, or null if it does not exist in this
     *         environment
     */
    Node<T> getNodeByID(int id);

    /**
     * All the nodes that exist in current environment.
     * 
     * @return All the nodes that exist in current environment
     */
    Collection<? extends Node<T>> getNodes();

    /**
     * @return the number of nodes currently in the environment
     */
    int getNodesNumber();

    /**
     * Given a node (center) this method returns a list of all the surroundings
     * nodes within the given range. Note that this method (depending on the
     * implementation) might be not optimized and it's consequently <b>much</b>
     * better to use {@link Environment#getNeighborhood(Node)} and filter the
     * neighborhood if you are sure that all the nodes within the range are
     * connected to the center.
     * 
     * @param center
     *            the node to consider as center
     * @param range
     *            the exploration range
     * @return the list of nodes within the range
     */
    Collection<Node<T>> getNodesWithinRange(Node<T> center, double range);

    /**
     * Given a {@link Position}(center) this method returns a list of all the
     * surroundings nodes within the given range. Note that this method
     * (depending on the implementation) might be not optimized.
     * 
     * @param center
     *            the {@link Position} to consider as center
     * @param range
     *            the exploration range
     * @return the list of nodes within the range
     */
    Collection<Node<T>> getNodesWithinRange(Position center, double range);

    /**
     * This method allows to know which are the smallest coordinates
     * represented.
     * 
     * @return an array of length getDimensions() containing the smallest
     *         coordinates for each dimension.
     */
    double[] getOffset();

    /**
     * Calculates the position of a node.
     * 
     * @param node
     *            the node you want to know the position
     * @return The position
     */
    Position getPosition(Node<T> node);

    /**
     * @return the class name of the monitor that should preferably used for
     *         displaying the contents of this environment. Full class names are
     *         recommended: it's up to the graphical interface to decide how to
     *         deal with simple class names (whether or not to prefix a standard
     *         package). In case of null return or inexistent class, the default
     *         monitor will be used.
     */
    String getPreferredMonitor();

    /**
     * This method returns the size of the environment as an array of length
     * getDimensions.
     * 
     * @return the size of this environment
     */
    double[] getSize();

    /**
     * This method moves a node in the environment toward some direction. If
     * node move is unsupported, it does nothing.
     * 
     * @param node
     *            The node to move
     * @param direction
     *            The position which will be summed to the current position to
     *            move the node in the right place.
     */
    void moveNode(Node<T> node, Position direction);

    /**
     * This method moves a node in the environment to some position. If node
     * move is unsupported, it does nothing.
     * 
     * @param node
     *            The node to move
     * @param position
     *            The absolute position in which this node will be moved.
     */
    void moveNodeToPosition(Node<T> node, Position position);

    /**
     * This method allows to remove a node. If node removal is unsupported, it
     * does nothing.
     * 
     * @param node
     *            the node to remove
     */
    void removeNode(Node<T> node);

    /**
     * @param rule
     *            the rule to set
     */
    void setLinkingRule(LinkingRule<T> rule);

    /**
     * @return the current linking rule
     */
    LinkingRule<T> getLinkingRule();

    /**
     * Add a {@link Layer} to the {@link Environment}.
     * @param l the {@link Layer}
     */
    void addLayer(Layer<T> l);

    /**
     * Return all the Layers in this {@link Environment}.
     * @return a {@link List} of {@link Layer}.
     */
    Set<Layer<T>> getLayers();

}
