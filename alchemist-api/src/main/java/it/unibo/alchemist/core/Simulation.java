/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.interfaces.Actionable;
import org.jooq.lambda.fi.lang.CheckedRunnable;

import it.unibo.alchemist.boundary.OutputMonitor;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * This interface forces simulations to be independent threads, and make them
 * controllable from an external console.
 *
 * @param <T> Concentration type
 * @param <P> Position Type
 */
public interface Simulation<T, P extends Position<? extends P>> extends Runnable {

    /**
     * Adds an {@link OutputMonitor} to this simulation.
     *
     * @param op the OutputMonitor to add
     */
    void addOutputMonitor(OutputMonitor<T, P> op);

    /**
     * Allows to access the current environment.
     *
     * @return a reference to the current Environment. The environment is not a
     *         copy but back-ends the real environment used in the simulation.
     *         Manipulate it carefully
     */
    Environment<T, P> getEnvironment();

    /**
     * @return an {@link Optional} containing the exception that made the
     *         simulation fail, or {@link Optional#empty()} in case the
     *         simulation is ongoing or has terminated successfully.
     */
    Optional<Throwable> getError();

    /**
     * @return the step at which this simulation will eventually stop.
     */
    long getFinalStep();

    /**
     * Allows to at which time this simulation will end.
     *
     * @return the final time
     */
    Time getFinalTime();

    /**
     * Allows to access the current status.
     *
     * @return the current Status of the simulation
     */
    Status getStatus();

    /**
     * Allows to access the current simulation step.
     *
     * @return the current step
     */
    long getStep();

    /**
     * Allows to know which is the current simulation time.
     *
     * @return the current time
     */
    Time getTime();

    /**
     * Executes a certain number of steps, then pauses it.
     *
     * @param steps the number of steps to execute
     */
    void goToStep(long steps);

    /**
     * Executes the simulation until the target time is reached, then pauses it.
     *
     * @param t the target time
     */
    void goToTime(Time t);

    /**
     * This method must get called in case a a communication link connecting two
     * nodes gets created during the simulation. This method provides dependency
     * and scheduling times re-computation for all the reactions interested by
     * such change.
     *
     * @param node the node
     * @param n    the second node
     */
    void neighborAdded(Node<T> node, Node<T> n);

    /**
     * This method must get called in case a a communication link connecting two
     * nodes gets broken during the simulation. This method provides dependency
     * and scheduling times re-computation for all the reactions interested by
     * such change.
     *
     * @param node the node
     * @param n    the second node
     */
    void neighborRemoved(Node<T> node, Node<T> n);

    /**
     * This method must get called in case a node is added to the environment
     * during the simulation and after its neighborhood has been computed (or
     * can be consistently computed by the simulated environment). This method
     * provides dependency computation and is responsible of correctly
     * scheduling the Node's new reactions.
     *
     * @param node the freshly added node
     * @throws IllegalMonitorStateException
     *             if the method gets called from a different thread than the
     *             simulation thread
     */
    void nodeAdded(Node<T> node);

    /**
     * This method must get called in case a node is moved in the environment
     * during the simulation and after its neighborhood has been computed (or
     * can be consistently computed by the simulated environment). This method
     * provides dependency computation and is responsible of correctly
     * scheduling the Node's reactions.
     *
     * @param node the node
     */
    void nodeMoved(Node<T> node);

    /**
     * This method must get called in case a node is removed from the
     * environment during the simulation and after its neighborhood has been
     * computed (or can be consistently computed by the simulated environment).
     * This method provides dependency computation and is responsible of
     * correctly removing the Node's reactions from the scheduler.
     *
     * @param node            the freshly removed node
     * @param oldNeighborhood the neighborhood of the node as it was before it was removed
     *                        (used to calculate reverse dependencies)
     */
    void nodeRemoved(Node<T> node, Neighborhood<T> oldNeighborhood);

    /**
     * Sends a pause command to the simulation.
     * There is no guarantee on when this command will be actually processed.
     */
    void pause();

    /**
     * Sends a play command to the simulation.
     * There is no guarantee on when this command will be actually processed.
     */
    void play();

    /**
     * Adds a reaction during the simulation to the scheduler and start to execute it.
     * The reaction addition is not propagated in the {@link Node} entity.
     * To do that call also the method {@link Node#addReaction(Reaction)}.
     * @param reactionToAdd the reaction to add
     */
    void reactionAdded(Actionable<T> reactionToAdd);

    /**
     * Removes a reaction during the simulation from the scheduler and stop to execute it.
     * The reaction removal is not propagated in the {@link Node} entity.
     * To do that call also the method {@link Node#removeReaction(Reaction)}.
     * @param reactionToRemove the reaction to remove
     */
    void reactionRemoved(Actionable<T> reactionToRemove);

    /**
     * Removes an {@link OutputMonitor} to this simulation. If the
     * {@link OutputMonitor} was not among those already added, this method does
     * nothing.
     *
     * @param op the OutputMonitor to add
     */
    void removeOutputMonitor(OutputMonitor<T, P> op);

    /**
     * Schedules a runnable to be executed by the Simulation thread, useful for
     * synchronization purposes (e.g. make sure that the environment is not
     * being changed while the requested operation is being executed). An
     * exception thrown by the passed runnable will make the simulation
     * terminate.
     *
     * @param r the runnable to execute
     */
    void schedule(CheckedRunnable r);

    /**
     * Sends a terminate command to the simulation.
     * There is no guarantee on when this command will be actually processed.
     */
    void terminate();

    /**
     * Suspends the caller until the simulation reaches the selected {@link Status} or the timeout ends.
     *
     * Please note that waiting for a status does not mean that every {@link OutputMonitor} will already be
     * notified of the update.
     *
     * @param s        The {@link Status} the simulation should reach before returning from this method
     * @param timeout  The maximum lapse of time the caller wants to wait before being resumed
     * @param timeunit The {@link TimeUnit} used to define "timeout"
     *
     * @return the status of the Simulation at the end of the wait
     */
    Status waitFor(Status s, long timeout, TimeUnit timeunit);

}
