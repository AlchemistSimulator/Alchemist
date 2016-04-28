/**
 * 
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.implementations.strategies.routing.OnStreets;
import it.unibo.alchemist.model.implementations.strategies.speed.InteractWithOthers;
import it.unibo.alchemist.model.implementations.strategies.target.FollowTarget;
import it.unibo.alchemist.model.interfaces.IMapEnvironment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Vehicle;

/**
 *
 * @param <T>
 */
public class TargetWalker<T> extends MoveOnMap<T> {

    /**
     * Default speed in meters per second.
     */
    public static final double DEFAULT_SPEED = 1.5;
    /**
     * Default interaction range.
     */
    public static final double DEFAULT_RANGE = 0;
    /**
     * Default interaction factor.
     */
    public static final double DEFAULT_INTERACTION = 0;

    private static final long serialVersionUID = 5097382908560832035L;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction. Will be used to compute the distance to walk in
     *            every step, relying on {@link Reaction}'s getRate() method.
     * @param trackMolecule
     *            the molecule to track. Its value will be read when it is time
     *            to compute a new target. If it is a {@link LatLongPosition},
     *            it will be used as-is. If it is an {@link Iterable}, the first
     *            two values (if they are present and they are numbers, or
     *            Strings parse-able to numbers) will be used to create a new
     *            {@link LatLongPosition}. Otherwise, the {@link Object} bound
     *            to this {@link Molecule} will be converted to a String, and
     *            the String will be parsed using the float regular expression
     *            matcher in Javalib.
     * @param interactingMolecule
     *            the molecule that decides wether or not a node is physically
     *            interacting with the node in which this action is executed,
     *            slowing this node down. The node will be considered
     *            "interacting" if such molecule is present, regardless its
     *            value.
     * @param speed
     *            the speed at which this {@link MoveOnMap} will move
     * @param interaction
     *            the higher, the more the {@link MoveOnMap} slows down
     *            when obstacles are found
     * @param range
     *            the range in which searching for possible obstacles. Obstacles
     *            slow down the {@link MoveOnMap}
     */
    public TargetWalker(final IMapEnvironment<T> environment, final Node<T> node, final Reaction<T> reaction,
            final Molecule trackMolecule, final Molecule interactingMolecule, final double speed, final double interaction,
            final double range) {
        super(environment, node,
                new OnStreets<>(environment, Vehicle.FOOT),
                new InteractWithOthers<>(environment, node, reaction, interactingMolecule, speed, range, interaction),
                new FollowTarget<>(environment, node, trackMolecule));
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction. Will be used to compute the distance to walk in
     *            every step, relying on {@link Reaction}'s getRate() method.
     * @param trackMolecule
     *            the molecule to track. Its value will be read when it is time
     *            to compute a new target. If it is a {@link LatLongPosition},
     *            it will be used as-is. If it is an {@link Iterable}, the first
     *            two values (if they are present and they are numbers, or
     *            Strings parse-able to numbers) will be used to create a new
     *            {@link LatLongPosition}. Otherwise, the {@link Object} bound
     *            to this {@link Molecule} will be converted to a String, and
     *            the String will be parsed using the float regular expression
     *            matcher in Javalib.
     * @param interactingMolecule
     *            the molecule that decides wether or not a node is physically
     *            interacting with the node in which this action is executed,
     *            slowing this node down. The node will be considered
     *            "interacting" if such molecule is present, regardless its
     *            value.
     * @param speed
     *            the speed at which this {@link MoveOnMap} will move
     *            when obstacles are found
     */
    public TargetWalker(final IMapEnvironment<T> environment, final Node<T> node, final Reaction<T> reaction,
            final Molecule trackMolecule, final Molecule interactingMolecule, final double speed) {
        this(environment, node, reaction, trackMolecule, interactingMolecule, speed, DEFAULT_INTERACTION, DEFAULT_RANGE);
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction. Will be used to compute the distance to walk in
     *            every step, relying on {@link Reaction}'s getRate() method.
     * @param trackMolecule
     *            the molecule to track. Its value will be read when it is time
     *            to compute a new target. If it is a {@link LatLongPosition},
     *            it will be used as-is. If it is an {@link Iterable}, the first
     *            two values (if they are present and they are numbers, or
     *            Strings parse-able to numbers) will be used to create a new
     *            {@link LatLongPosition}. Otherwise, the {@link Object} bound
     *            to this {@link Molecule} will be converted to a String, and
     *            the String will be parsed using the float regular expression
     *            matcher in Javalib.
     * @param interactingMolecule
     *            the molecule that decides wether or not a node is physically
     *            interacting with the node in which this action is executed,
     *            slowing this node down. The node will be considered
     *            "interacting" if such molecule is present, regardless its
     *            value.
     */
    public TargetWalker(final IMapEnvironment<T> environment, final Node<T> node, final Reaction<T> reaction,
            final Molecule trackMolecule, final Molecule interactingMolecule) {
        this(environment, node, reaction, trackMolecule, interactingMolecule, DEFAULT_SPEED);
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction. Will be used to compute the distance to walk in
     *            every step, relying on {@link Reaction}'s getRate() method.
     * @param trackMolecule
     *            the molecule to track. Its value will be read when it is time
     *            to compute a new target. If it is a {@link LatLongPosition},
     *            it will be used as-is. If it is an {@link Iterable}, the first
     *            two values (if they are present and they are numbers, or
     *            Strings parse-able to numbers) will be used to create a new
     *            {@link LatLongPosition}. Otherwise, the {@link Object} bound
     *            to this {@link Molecule} will be converted to a String, and
     *            the String will be parsed using the float regular expression
     *            matcher in Javalib.
     * @param interactingMolecule
     *            the molecule that decides wether or not a node is physically
     *            interacting with the node in which this action is executed,
     *            slowing this node down. The node will be considered
     *            "interacting" if such molecule is present, regardless its
     *            value.
     * @param speed
     *            the speed at which this {@link MoveOnMap} will move
     * @param interaction
     *            the higher, the more the {@link MoveOnMap} slows down
     *            when obstacles are found
     * @param range
     *            the range in which searching for possible obstacles. Obstacles
     *            slow down the {@link MoveOnMap}
     */
    public TargetWalker(final IMapEnvironment<T> environment, final Node<T> node, final Reaction<T> reaction,
            final String trackMolecule, final String interactingMolecule, final double speed, final double interaction,
            final double range) {
        this(environment, node, reaction, new SimpleMolecule(trackMolecule), new SimpleMolecule(interactingMolecule), speed, interaction, range);
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction. Will be used to compute the distance to walk in
     *            every step, relying on {@link Reaction}'s getRate() method.
     * @param trackMolecule
     *            the molecule to track. Its value will be read when it is time
     *            to compute a new target. If it is a {@link LatLongPosition},
     *            it will be used as-is. If it is an {@link Iterable}, the first
     *            two values (if they are present and they are numbers, or
     *            Strings parse-able to numbers) will be used to create a new
     *            {@link LatLongPosition}. Otherwise, the {@link Object} bound
     *            to this {@link Molecule} will be converted to a String, and
     *            the String will be parsed using the float regular expression
     *            matcher in Javalib.
     * @param interactingMolecule
     *            the molecule that decides wether or not a node is physically
     *            interacting with the node in which this action is executed,
     *            slowing this node down. The node will be considered
     *            "interacting" if such molecule is present, regardless its
     *            value.
     * @param speed
     *            the speed at which this {@link MoveOnMap} will move
     */
    public TargetWalker(final IMapEnvironment<T> environment, final Node<T> node, final Reaction<T> reaction,
            final String trackMolecule, final String interactingMolecule, final double speed) {
        this(environment, node, reaction, trackMolecule, interactingMolecule, speed, DEFAULT_INTERACTION, DEFAULT_RANGE);
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction. Will be used to compute the distance to walk in
     *            every step, relying on {@link Reaction}'s getRate() method.
     * @param trackMolecule
     *            the molecule to track. Its value will be read when it is time
     *            to compute a new target. If it is a {@link LatLongPosition},
     *            it will be used as-is. If it is an {@link Iterable}, the first
     *            two values (if they are present and they are numbers, or
     *            Strings parse-able to numbers) will be used to create a new
     *            {@link LatLongPosition}. Otherwise, the {@link Object} bound
     *            to this {@link Molecule} will be converted to a String, and
     *            the String will be parsed using the float regular expression
     *            matcher in Javalib.
     * @param interactingMolecule
     *            the molecule that decides wether or not a node is physically
     *            interacting with the node in which this action is executed,
     *            slowing this node down. The node will be considered
     *            "interacting" if such molecule is present, regardless its
     *            value.
     */
    public TargetWalker(final IMapEnvironment<T> environment, final Node<T> node, final Reaction<T> reaction,
            final String trackMolecule, final String interactingMolecule) { 
        this(environment, node, reaction, trackMolecule, interactingMolecule, DEFAULT_SPEED);
    }

}
