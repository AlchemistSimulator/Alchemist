/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Environment2DWithObstacles;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Obstacle2D;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * @param <P>
 */
public abstract class SocialForceAgent<P extends Position<P>> extends SAPEREMoveNodeAgent<P> {

    private static final ILsaMolecule ACTIVE = new LsaMolecule("active");
    /**
     * The proximity deceleration factor.
     */
    public static final double DECELERATION_FACTOR = 0.7;
    /**
     * Step time interval.
     */
    public static final double DELTA_T = 0.01;
    /**
     * Desired force (the attractive force contribution from the target node)
     * weight factor.
     */
    public static final double DESIRED_FORCE_FACTOR = 1.0;
    /**
     * Dodge force (the turn repulsive force contribution) weight factor.
     */
    public static final double DODGE_FORCE_FACTOR = 10.0;
    /**
     * The dodge strength.
     */
    public static final double DODGE_FORCE_STRENGTH = 2.0;
    /**
     * The strength of the pedestrian repulsive force.
     */
    public static final double ETA = 0.2;
    /**
     * A pedestrian can move at most of LIMIT along each axis.
     */
    private static final double LIMIT = 0.1;
    /**
     * Minimum Displacement Cycle Threshold: used to stop a pedestrian if he
     * can't move enough for a certain number of cycles.
     */
    public static final int MIN_DISP_CYC_TH = 200;
    /**
     * Minimum Displacement.
     */
    public static final double MIN_DISPLACEMENT = 0.01;
    /**
     * A minimum distance value used to check if a pedestrian has reached the
     * target node.
     */
    public static final double MIN_PHEROMONE_DISTANCE = 0.5;
    /**
     * Momentum factor used to compute the new speed value.
     */
    public static final double MOMENTUM_FACTOR = 0.75;
    /**
     * OLD_W and NEW_W are weights used to compute the new direction as a
     * weighted average starting from old and freshly computed directions. NEW_W
     * is the freshly computed direction weight.
     */
    public static final double NEW_W = 1.0;
    /**
     * Obstacle force (the repulsive force contribution from obstacles) weight
     * factor.
     */
    public static final double OBSTACLE_FORCE_FACTOR = 2.0;
    /**
     * The obstacle force strength.
     */
    public static final double OBSTACLE_FORCE_STRENGTH = 0.6;
    /**
     * The pedestrian interaction range with obstacles.
     */
    public static final double OBSTACLE_INTERACTION_RANGE = 0.48;
    /**
     * OLD_W and NEW_W are weights used to compute the new direction as a
     * weighted average starting from old and freshly computed directions. OLD_W
     * is the old direction weight.
     */
    public static final double OLD_W = 5.0;
    /**
     * The probability of choosing a pejorative move.
     */
    public static final double PEJORATIVE_MOVE_PROBABILITY = 0.2;

    /**
     * Molecule used to check if a node is a person.
     */
    private static final ILsaMolecule PERSON = new LsaMolecule("person");
    /**
     * Range in which a pedestrian will decelerate because of the presence of
     * other pedestrians.
     */
    public static final double PROXIMITY_DEC_RANGE = 0.5;
    /**
     * Range in which a pedestrian will turn because of the presence of other
     * pedestrians.
     */
    public static final double PROXIMITY_TURN_RANGE = 0.5;
    /**
     * 
     */
    private static final long serialVersionUID = 228276533881360456L;
    /**
     * Social force (the repulsive force contribution from other pedestrians)
     * weight factor.
     */
    public static final double SOCIAL_FORCE_FACTOR = 5.0;

    /**
     * The next best node.
     */
    private Node<List<ILsaMolecule>> bestNode;
    /**
     * The proximity deceleration factor.
     */
    private final double decelerationFactor;
    /* SIMULATION PARAMETERS */
    /**
     * Step time interval.
     */
    private final double deltaT;
    /**
     * Desired force (the attractive force contribution from the target node)
     * weight factor.
     */
    private final double desiredForceFactor;
    /**
     * Dodge force (the turn repulsive force contribution) weight factor.
     */
    private final double dodgeForceFactor;
    /**
     * The dodge strength.
     */
    private final double dodgeForceStrength;
    /**
     * Double values used to store the chosen direction at each cycle. These
     * values are used to compute the new directions as a weighted average
     * between the old and freshly computed directions.
     */
    private double dxOld, dyOld;
    /**
     * The environment in which the simulation exists.
     */
    private final Environment<List<ILsaMolecule>, P> env;
    /**
     * The strength of the pedestrian repulsive force.
     */
    private final double eta;
    /**
     * Control variable used to execute a minimum gradient value motion or a
     * maximum pheromone value motion.
     */
    private boolean getMinimumGradient = true;
    /**
     * Position of the gradient value inside the tuple.
     */
    private final int gradDistPos;
    /**
     * The integer group identifier.
     */
    private final int groupId;
    /**
     * Range in which a pedestrian interact with other pedestrian: is the sum
     * between pedestrian dimension and desired space.
     */
    private final double interactionRange;
    /**
     * Minimum Displacement Cycle Threshold: used to stop a pedestrian if he
     * can't move enough for a certain number of cycles.
     */
    private final int minDispCycTh;
    /**
     * Minimum Displacement.
     */
    private final double minDisplacement;
    /**
     * Integer variable used to count number of subsequent cycles in which an
     * agent can't perform a "real" movement.
     */
    private int minDisplacementCycleCount;

    /**
     * A minimum distance value used to check if a pedestrian has reached the
     * target node.
     */
    private final double minpheromoneDistance;
    /**
     * Momentum factor used to compute the new speed value.
     */
    private final double momentumFactor;
    private final ILsaMolecule newPheromone;
    /**
     * OLD_W and NEW_W are weights used to compute the new direction as a
     * weighted average starting from old and freshly computed directions. NEW_W
     * is the freshly computed direction weight.
     */
    private final double newW;
    /**
     * Obstacle force (the repulsive force contribution from obstacles) weight
     * factor.
     */
    private final double obstacleForceFactor;
    /**
     * The obstacle force strength.
     */
    private final double obstacleForceStrength;
    /**
     * The pedestrian interaction range with obstacles.
     */
    private final double obstacleInteractionRange;
    /**
     * OLD_W and NEW_W are weights used to compute the new direction as a
     * weighted average starting from old and freshly computed directions. OLD_W
     * is the old direction weight.
     */
    private final double oldW;
    /**
     * The probability of choosing a pejorative move.
     */
    private final double pejorativeMoveProbability;
    private final ILsaMolecule pheromoneTmpl;
    /**
     * Range in which a pedestrian will decelerate because of the presence of
     * other pedestrians.
     */
    private final double proximityDecelerationRange;
    /**
     * Range in which a pedestrian will turn because of the presence of other
     * pedestrians.
     */
    private final double proximityTurnRange;
    private final RandomGenerator rs;
    /**
     * Social force (the repulsive force contribution from other pedestrians)
     * weight factor.
     */
    private final double socialForceFactor;
    /**
     * Flag used to let the user chose to make pedestrians to stop once the
     * target is reached.
     */
    private final boolean stopAtTarget;
    /**
     * Boolean control variable used to check if the target node is in line of
     * sight or not.
     */
    private boolean targetInLineOfSight;
    /**
     * The next target position.
     */
    private P targetPositions;
    /**
     * Molecule template corresponding to the tuple from which read gradients
     * value.
     */
    private final ILsaMolecule template;

    /**
     * The probability of turn to the right if another pedestrian is too near.
     */
    private final double turnRightProbability;

    /**
     * List that contains all already visited node.
     */
    private final List<ILsaNode> visitedNodes = new ArrayList<>();

    /**
     * Maximum movement speed of a pedestrian.
     */
    private final double vmax;

    /**
     * Speed vector: x and y speed components.
     */
    private double vx;

    private double vy;

    /**
     * Construct a pedestrian agent. with
     * 
     * @param environment
     *            the current environment
     * @param node
     *            the current node
     * @param random
     *            the current random engine
     * @param molecule
     *            the LSA to inspect once moving (typically a gradient)
     * @param pos
     *            the position in the LSA of the value to read for identifying
     *            the new position
     * @param group
     *            the group identifier: if equals to 0 there is no group
     * @param stopWhenAtTarget
     *            flag used to let the user chose to make pedestrians to stop
     *            once the target is reached
     * @param vmaxval
     *            - maximum movement speed of the pedestrian
     * @param mydimensionval
     *            - pedestrian size
     * @param desiredspaceval
     *            - pedestrian desired space
     * @param turnrightprobabilityval
     *            - the probability of turn to the right
     */
    public SocialForceAgent(final Environment<List<ILsaMolecule>, P> environment, final ILsaNode node, final RandomGenerator random, final LsaMolecule molecule, final int pos, final int group, final boolean stopWhenAtTarget, final double vmaxval, final double mydimensionval, final double desiredspaceval, final double turnrightprobabilityval) {
        this(environment, node, random, molecule, pos, group, stopWhenAtTarget, DELTA_T, vmaxval, MOMENTUM_FACTOR, OLD_W, NEW_W, mydimensionval, desiredspaceval, PROXIMITY_DEC_RANGE, PROXIMITY_TURN_RANGE, DESIRED_FORCE_FACTOR, SOCIAL_FORCE_FACTOR, ETA, DODGE_FORCE_FACTOR, DODGE_FORCE_STRENGTH, turnrightprobabilityval, OBSTACLE_FORCE_FACTOR, OBSTACLE_INTERACTION_RANGE, OBSTACLE_FORCE_STRENGTH, PEJORATIVE_MOVE_PROBABILITY, MIN_PHEROMONE_DISTANCE, DECELERATION_FACTOR, MIN_DISPLACEMENT, MIN_DISP_CYC_TH);

    }

    /**
     * Construct a pedestrian agent. with
     * 
     * @param environment
     *            the current environment
     * @param node
     *            the current node
     * @param random
     *            the current random engine
     * @param molecule
     *            the LSA to inspect once moving (typically a gradient)
     * @param pos
     *            the position in the LSA of the value to read for identifying
     *            the new position
     * @param group
     *            the group identifier: if equals to 0 there is no group
     * @param stopWhenAtTarget
     *            flag used to let the user chose to make pedestrians to stop
     *            once the target is reached
     * @param dt
     *            - step time interval
     * @param vMax
     *            - maximum movement speed of the pedestrian
     * @param momentum
     *            - momentum factor used to compute the new speed value
     * @param oldw
     *            - old direction weight used to compute the new direction as a
     *            weighted average
     * @param neww
     *            - new direction weight used to compute the new direction as a
     *            weighted average
     * @param dimension
     *            - pedestrian size
     * @param space
     *            - pedestrian desired space
     * @param decRange
     *            - range in which a pedestrian will decelerate because of the
     *            presence of other pedestrians
     * @param turnRange
     *            - range in which a pedestrian will turn because of the
     *            presence of other pedestrians
     * @param desiredFactor
     *            - desired force weight factor
     * @param socialFactor
     *            - social force weight factor
     * @param etaV
     *            - social force strength
     * @param dodgeFactor
     *            - dodge force weight factor
     * @param dodgeStrength
     *            - dodge force strength
     * @param rightProbability
     *            - the probability of turn to the right
     * @param obstacleFactor
     *            - obstacle force weight factor
     * @param obstacleRange
     *            - pedestrian interaction range with obstacles
     * @param obstacleStrength
     *            - obstacle force strength
     * @param detrimentalProbability
     *            - the probability of choosing a detrimental move
     * @param minpheromoneDist
     *            - minimum distance value used to check if a pedestrian has
     *            reached the target node
     * @param decFactor
     *            - proximity deceleration factor
     * @param minDisp
     *            - minimum displacement
     * @param minDispCyc
     *            - threshold used to stop a pedestrian if he can't move enough
     *            for a certain number of cycles
     */
    public SocialForceAgent(final Environment<List<ILsaMolecule>, P> environment, final ILsaNode node, final RandomGenerator random, final LsaMolecule molecule, final int pos, final int group, final boolean stopWhenAtTarget, final double dt, final double vMax, final double momentum, final double oldw, final double neww, final double dimension, final double space, final double decRange, final double turnRange, final double desiredFactor, final double socialFactor, final double etaV, final double dodgeFactor, final double dodgeStrength, final double rightProbability, final double obstacleFactor, final double obstacleRange, final double obstacleStrength, final double detrimentalProbability, final double minpheromoneDist, final double decFactor, final double minDisp, final int minDispCyc) {

        super(environment, node);

        this.env = environment;
        this.template = molecule;
        this.gradDistPos = pos;
        this.groupId = group;
        this.stopAtTarget = stopWhenAtTarget;
        rs = random;

        /* SIMULATION PARAMETERS */
        deltaT = dt;
        this.vmax = vMax; // in m/s between 1.0 (inclusive) and 2.0 (exclusive)
        momentumFactor = momentum;
        oldW = oldw;
        newW = neww;
        interactionRange = space + dimension;
        proximityDecelerationRange = decRange;
        proximityTurnRange = turnRange;
        desiredForceFactor = desiredFactor;
        socialForceFactor = socialFactor;
        this.eta = etaV;
        dodgeForceFactor = dodgeFactor;
        dodgeForceStrength = dodgeStrength;
        turnRightProbability = rightProbability;
        obstacleForceFactor = obstacleFactor;
        obstacleInteractionRange = obstacleRange;
        obstacleForceStrength = obstacleStrength;
        pejorativeMoveProbability = detrimentalProbability;
        minpheromoneDistance = minpheromoneDist;
        decelerationFactor = decFactor;
        minDisplacement = minDisp;
        minDispCycTh = minDispCyc;
        newPheromone = new LsaMolecule("pheromone, " + 1 + ", " + groupId);
        pheromoneTmpl = new LsaMolecule("pheromone, Number, " + groupId);
    }

    /**
     * Method used in group behavior to check the distance between the best node
     * and the pedestrian node. If the pedestrian has reached the best node, in
     * the next cycle will be performed a minimum gradient node search
     */
    private void checkMaxpheromoneNodeDistance() {
        // Distance check
        final double dist = env.getDistanceBetweenNodes(bestNode, getNode());
        /*
         * If the best node is very near, set it as already visited (adding it
         * in a list)and select the minimum gradient value search for the next
         * cycle
         */
        if (dist < minpheromoneDistance) {
            getMinimumGradient = true;
            visitedNodes.add((ILsaNode) bestNode);
        } else {
            // Select the maximum pheromone value search
            getMinimumGradient = false;
        }
    }

    /**
     * Method used to compute the interaction between pedestrian. In particular
     * this method compute a force contribution representing the desire of the
     * pedestrian to turn to the left or to the right when he's too near to
     * another pedestrian.
     * 
     * @param neigh
     *            - the current pedestrian neighborhood
     * @param desiredForce
     *            - the previously computed attractive force contribution
     *            through the target
     * @param mypos
     *            - the current pedestrian position
     * @return dodgeForce: the desire of the pedestrian to dodge a neighbor
     */
    private P computeDodgeForce(final Neighborhood<List<ILsaMolecule>> neigh, final P desiredForce, final P mypos) {
        double dodgeForceX = 0.0;
        double dodgeForceY = 0.0;

        // For each node in the neighborhood
        for (final Node<List<ILsaMolecule>> node : neigh.getNeighbors()) {
            final ILsaNode n = (ILsaNode) node;
            // If the current node is a person
            if (n.getConcentration(PERSON).size() != 0) {
                final P pos = env.getPosition(n);
                // If the distance between me and the current person is less
                // than a minor range
                if (pos.getDistanceTo(mypos) < proximityTurnRange) {
                    /*
                     * The pedestrian has the 85% of probability to turn to
                     * the rightand the 15% of probability to turn to the
                     * left
                     */
                    final List<Reaction<List<ILsaMolecule>>> reactions = node.getReactions();
                    if (!reactions.isEmpty()) {
                        final Reaction<List<ILsaMolecule>> reaction = reactions.get(0);
                        final List<Action<List<ILsaMolecule>>> actions = reaction.getActions();
                        if (!actions.isEmpty()) {
                            final Action<List<ILsaMolecule>> action = actions.get(0);
                            @SuppressWarnings("unchecked")
                            final SocialForceAgent<P> currAgent = (SocialForceAgent<P>) action;
                            if ((vx > 0 && currAgent.getSpeed().getCartesianCoordinates()[0] < 0 && vy > 0 && currAgent.getSpeed().getCartesianCoordinates()[1] < 0) || (vx < 0 && currAgent.getSpeed().getCartesianCoordinates()[0] > 0 && vy < 0 && currAgent.getSpeed().getCartesianCoordinates()[1] > 0)) {
                                if (rs.nextDouble() >= turnRightProbability) {
                                    // turn left
                                    dodgeForceX = -dodgeForceStrength * desiredForce.getCartesianCoordinates()[1];
                                    dodgeForceY = dodgeForceStrength * desiredForce.getCartesianCoordinates()[0];
                                } else {
                                    // turn right
                                    dodgeForceX = dodgeForceStrength * desiredForce.getCartesianCoordinates()[1];
                                    dodgeForceY = -dodgeForceStrength * desiredForce.getCartesianCoordinates()[0];
                                }
                            }
                        }
                    }
                }
            }
        }

        return getEnvironment().makePosition(dodgeForceX, dodgeForceY);
    }

    /**
     * FIRST SOLUTION. Method used to compute the interaction (so the repulsive
     * force contribution) between pedestrians
     * 
     * @param desiredForce
     *            - the attractive force through the desired direction
     * @param myx
     *            - the x position coordinate of the pedestrian
     * @param myy
     *            - the y position coordinate of the pedestrian
     * @return socialForce - the repulsive force contribution from interactions
     *         with other pedestrians
     */
    public P computeInteractions(final P desiredForce, final double myx, final double myy) {
        double socialForceX = 0.0;
        double socialForceY = 0.0;

        final Neighborhood<List<ILsaMolecule>> neigh = getLocalNeighborhood();
        // For each node in the neighborhood
        for (final Node<List<ILsaMolecule>> node : neigh.getNeighbors()) {
            final ILsaNode n = (ILsaNode) node;
            final P pi = getPosition(n);
            // If the current node is a person and if is not me
            if (n.getConcentration(PERSON).size() != 0 && node.getId() != getNode().getId()) {
                /*
                 * Compute the repulsive force contribution if the current
                 * person is inside the agent'sinteraction range
                 */
                final double dist = env.getDistanceBetweenNodes(n, getNode());
                if (dist <= interactionRange) {
                    // STEP 1
                    final double ex = (pi.getCartesianCoordinates()[0] - myx) / dist;
                    final double ey = (pi.getCartesianCoordinates()[1] - myy) / dist;

                    // STEP 2
                    double vij = 0.0;
                    final List<Reaction<List<ILsaMolecule>>> reactions = node.getReactions();
                    if (!reactions.isEmpty()) {
                        final Reaction<List<ILsaMolecule>> reaction = reactions.get(0);
                        final List<Action<List<ILsaMolecule>>> actions = reaction.getActions();
                        if (!actions.isEmpty()) {
                            final Action<List<ILsaMolecule>> action = actions.get(0);
                            @SuppressWarnings("unchecked")
                            final SocialForceAgent<P> currAgent = (SocialForceAgent<P>) action;

                            vij = ((vx - currAgent.getSpeed().getCartesianCoordinates()[0]) * ex) + ((vy - currAgent.getSpeed().getCartesianCoordinates()[1]) * ey);
                            if (vij < 0) {
                                vij = 0.0;
                            }

                        }
                    }
                    // STEP 3
                    final double vi = Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2));
                    final double kijpart = (vx * ex) + (vy * ey);
                    double kij;
                    if (kijpart > 0 && vi != 0) {
                        kij = kijpart / vi;
                    } else {
                        kij = 0.0;
                    }
                    // STEP 4
                    final double desired = Math.sqrt(Math.pow(desiredForce.getCartesianCoordinates()[0], 2) + Math.pow(desiredForce.getCartesianCoordinates()[1], 2));
                    socialForceX += kij * (Math.pow(((eta * desired) + vij), 2) / (dist - interactionRange)) * ex;
                    socialForceY += kij * (Math.pow(((eta * desired) + vij), 2) / (dist - interactionRange)) * ey;
                }

            }
        }

        return getEnvironment().makePosition(socialForceX, socialForceY);
    }

    /**
     * Method used to compute the repulsive force contribution from obstacles.
     * 
     * @param myx
     *            - the current pedestrian x-coordinate position
     * @param myy
     *            - the current pedestrian y-coordinate position
     * @param mypos
     *            - the current pedestrian position
     * @return obstacleForce - the repulsive force contribution from obstacles
     */
    private P computeObstacleForce(final double myx, final double myy, final P mypos) {
        double obstacleForceX = 0.0;
        double obstacleForceY = 0.0;
        Environment2DWithObstacles<?, ?, ?> obstacleEnv;
        if (env instanceof Environment2DWithObstacles) {
            obstacleEnv = (Environment2DWithObstacles<?, ?, ?>) env;
            final List<?> obstacles = obstacleEnv.getObstaclesInRange(myx, myy, obstacleInteractionRange);
            Rectangle2D bounds;
            double minDist = Double.MAX_VALUE;
            Obstacle2D nearestObstacle = null;
            for (final Object obObj : obstacles) {
                final Obstacle2D ob = (Obstacle2D) obObj;
                bounds = ob.getBounds();
                final Euclidean2DPosition[] edge = getNearestEdge(myx, myy, bounds);
                P intersectionPoint = null;
                if (edge == null) {
                    return getEnvironment().makePosition(obstacleForceX, obstacleForceY);
                } else {
                    if (edge[1].getCartesianCoordinates()[0] - edge[0].getCartesianCoordinates()[0] == 0) {
                        intersectionPoint = env.makePosition(edge[0].getCartesianCoordinates()[0], myy);
                    } else if (edge[1].getCartesianCoordinates()[1] - edge[0].getCartesianCoordinates()[1] == 0) {
                        intersectionPoint = env.makePosition(myx, edge[0].getCartesianCoordinates()[0]);
                    }
                }
                final double dist = mypos.getDistanceTo(Objects.requireNonNull(intersectionPoint));
                if (dist < minDist) {
                    minDist = dist;
                    nearestObstacle = ob;
                }
            }
            if (nearestObstacle != null) {
                final double dx = myx - nearestObstacle.getBounds2D().getCenterX();
                final double dy = myy - nearestObstacle.getBounds2D().getCenterY();

                obstacleForceX = obstacleForceStrength * (dx / minDist);
                obstacleForceY = obstacleForceStrength * (dy / minDist);
            }
        }

        return getEnvironment().makePosition(obstacleForceX, obstacleForceY);
    }

    @Override
    public final void execute() {
        // Retrieve the local neighborhood
        final Neighborhood<List<ILsaMolecule>> neigh = getLocalNeighborhood();
        targetPositions = null;
        bestNode = null;

        // If a group doesn't exists
        if (groupId == 0) {
            // Execute only the minimum gradient value search
            minimumGradientSearch(neigh);
        } else { // If a group exist
            // If minimum gradient value search is selected
            if (getMinimumGradient) {
                // For each node in the neighborhood
                minimumGradientSearch(neigh);

                // If there aren't any minimum gradient node, return
                if (bestNode == null || bestNode.contains(ACTIVE)) {
                    return;
                }
                releasepheromone();
            } else {
                // If the maximum pheromone value search
                maximumpheromoneSearch(neigh);

                // If there aren't any minumum gradient node, return
                if (bestNode == null || bestNode.contains(ACTIVE)) {
                    /*
                     * TODO: the pedestrian may loop in here. This is a bug.
                     */
                    return;
                }
                checkMaxpheromoneNodeDistance();
            }
        }

        // If there aren't any minumum gradient node, return
        if (bestNode == null || bestNode.contains(ACTIVE)) {
            return;
        }

        if (targetPositions != null) {
            // Get target x and y coordinates
            final double x = targetPositions.getCartesianCoordinates()[0];
            final double y = targetPositions.getCartesianCoordinates()[1];
            double dx;
            double dy;
            double ax;
            double ay;
            // TARGET FORCE - Compute the target node attractive force
            // contribution
            final P mypos = getCurrentPosition();
            final double myx = mypos.getCartesianCoordinates()[0];
            final double myy = mypos.getCartesianCoordinates()[1];
            final double distancex = x - myx;
            final double distancey = y - myy;
            final double dist = env.getDistanceBetweenNodes(bestNode, getNode());
            final double targetForceX = distancex / dist; // vector components
            final double targetForceY = distancey / dist;
            // DESIRED FORCE - Compute the desired force starting from the
            // target force and the agent's speed
            final P desiredForce = getEnvironment().makePosition(targetForceX * vmax, targetForceY * vmax);
            // SOCIAL FORCE - Compute neighbors pedestrians repulsive force
            // contribution
            final P socialForce = computeInteractions(desiredForce, myx, myy);
            /*
             * DODGE FORCE - Compute the force contribution that makes
             * pedestrian turn rightor left in order to dodge other pedestrians
             */
            final P dodgeForce = computeDodgeForce(neigh, desiredForce, mypos);
            // OBSTACLE FORCE - Compute near obstacles repulsive force
            // contribution
            final P obstacleForce = computeObstacleForce(myx, myy, mypos);
            // Compute acceleration components as a sum between all forces
            // acting on the agent
            ax = desiredForceFactor * desiredForce.getCartesianCoordinates()[0] + socialForceFactor * socialForce.getCartesianCoordinates()[0] + dodgeForceFactor * dodgeForce.getCartesianCoordinates()[0] + obstacleForceFactor * obstacleForce.getCartesianCoordinates()[0];
            ay = desiredForceFactor * desiredForce.getCartesianCoordinates()[1] + socialForceFactor * socialForce.getCartesianCoordinates()[1] + dodgeForceFactor * dodgeForce.getCartesianCoordinates()[1] + obstacleForceFactor * obstacleForce.getCartesianCoordinates()[1];
            // Compute new speed components
            vx = momentumFactor * vx + ax;
            vy = momentumFactor * vy + ay;
            // Check if new speed is greater than max speed, adjust it
            final double speed = Math.sqrt(vx * vx + vy * vy);
            if (speed > vmax) {
                vx = (vx / speed) * vmax; // compute the vector components
                vy = (vy / speed) * vmax;
            }
            // Compute displacement components
            dx = deltaT * vx;
            dy = deltaT * vy;
            // DIRECTION ADJUSTMENT
            /*
             * Check if both new displacement components aren't opposite to
             * previous displacement components.So an agent ignore those
             * displacement that lead him to make a step back.
             */
            if ((Double.longBitsToDouble(Double.doubleToRawLongBits(dxOld) ^ Double.doubleToRawLongBits(dx))) < 0 && (Double.longBitsToDouble(Double.doubleToRawLongBits(dyOld) ^ Double.doubleToRawLongBits(dy))) < 0) {
                if (rs.nextDouble() > 1 - pejorativeMoveProbability) {
                    dx = 0;
                    dy = 0;
                }
            } else {
                // Compute the new direction components as a weighted average
                // between old and freshly computed direction components.
                dx = (dxOld * oldW + dx * newW) / (oldW + newW);
                dy = (dyOld * oldW + dy * newW) / (oldW + newW);
            }
            // Store new direction components for the next cycle
            dxOld = dx;
            dyOld = dy;
            // BODY-TO-BODY INTERACION ADJUSTMENT
            // For each node in the neighborhood
            for (final Node<List<ILsaMolecule>> node : neigh.getNeighbors()) {
                final ILsaNode n = (ILsaNode) node;
                // If the current node is a person
                if (n.getConcentration(PERSON).size() != 0) {
                    final P pos = env.getPosition(n);
                    double xOther, yOther;
                    // If the distance between me and the current person is
                    // less than a certain range
                    if (pos.getDistanceTo(mypos) < proximityDecelerationRange) {
                        xOther = pos.getCartesianCoordinates()[0];
                        yOther = pos.getCartesianCoordinates()[1];
                        // If the current person is in front of me, so
                        // decelerate
                        if ((dx > 0 && xOther > myx) || (dx < 0 && xOther < myx)) {
                            dx = dx * decelerationFactor;
                        }
                        if ((dy > 0 && yOther > myy) || (dy < 0 && yOther < myy)) {
                            dy = dy * decelerationFactor;
                        }
                    }
                }
            }

            // STOPPING THE MOVEMENT AT THE DESTINATION
            // If the final target node is in line of sight
            if (stopAtTarget && targetInLineOfSight) {
                /*
                 * If the displacement is too little means that the agent is
                 * very near to the destination butcan't proceed because of the
                 * presence of other pedestrians
                 */
                if (dx < minDisplacement && dx > -minDisplacement && dy < minDisplacement && dy > -minDisplacement) {
                    minDisplacementCycleCount++;
                }
                // If the count of cycles in which the displacement is
                // negligible is greater than a certain threshold
                if (minDisplacementCycleCount > minDispCycTh) {
                    // Don't move anymore
                    dx = 0;
                    dy = 0;
                }
            }

            // If displacement components are greater than zero, check if they
            // aren't over the limit
            dx = dx > 0 ? Math.min(LIMIT, dx) : Math.max(-LIMIT, dx);
            dy = dy > 0 ? Math.min(LIMIT, dy) : Math.max(-LIMIT, dy);

            // If displacement components are greater than zero perform a
            // movement
            final boolean moveH = dx > 0 || dx < 0;
            final boolean moveV = dy > 0 || dy < 0;
            if (moveH || moveV) {
                move(getEnvironment().makePosition(moveH ? dx : 0, moveV ? dy : 0));
            }
        }
    }

    /**
     * Method used to retrieve the obstacle edge nearest to the pedestrian.
     * 
     * @param myx
     *            - the current pedestrian x-coordinate position
     * @param myy
     *            - the current pedestrian y-coordinate position
     * @param bounds
     *            - the rectangle obstacle bounds
     * @return nearest - an array containing two Continuous2DEuclidean object
     *         representing nearest edge vertices
     */
    private Euclidean2DPosition[] getNearestEdge(final double myx, final double myy, final Rectangle2D bounds) {

        Euclidean2DPosition[] nearest = null;

        final double minX = bounds.getMinX();
        final double maxX = bounds.getMaxX();
        final double minY = bounds.getMinY();
        final double maxY = bounds.getMaxY();

        if (myx > minX && myx < maxX) {
            if (myy > maxY) { // above the obstacle
                nearest = new Euclidean2DPosition[] { new Euclidean2DPosition(bounds.getMinX(), bounds.getMaxY()), new Euclidean2DPosition(bounds.getMaxX(), bounds.getMaxY()) };
            } else if (myy < minY) { // under the obstacle
                nearest = new Euclidean2DPosition[] { new Euclidean2DPosition(bounds.getMinX(), bounds.getMinY()), new Euclidean2DPosition(bounds.getMaxX(), bounds.getMinY()) };
            }
        } else if (myy > minY && myy < maxY) {
            if (myx < minX) { // to the left the obstacle
                nearest = new Euclidean2DPosition[] { new Euclidean2DPosition(bounds.getMinX(), bounds.getMaxY()), new Euclidean2DPosition(bounds.getMinX(), bounds.getMinY()) };
            } else if (myx > maxX) { // to the right the obstacle
                nearest = new Euclidean2DPosition[] { new Euclidean2DPosition(bounds.getMaxX(), bounds.getMaxY()), new Euclidean2DPosition(bounds.getMaxX(), bounds.getMinY()) };
            }
        }

        return nearest;
    }

    /**
     * Get speed of this agent.
     * 
     * @return - v: contains x and y speed components
     */
    public P getSpeed() {
        return getEnvironment().makePosition(vx, vy);
    }

    /**
     * Check if the input node is contained in the visited node list.
     * 
     * @param node
     *            - the node to check
     * @return true if the input node is in the list, false otherwise
     */
    private boolean isVisited(final ILsaNode node) {
        for (final ILsaNode n : visitedNodes) {
            if (n.getId() == node.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method used in group behavior to execute a maximum pheromone node search.
     * 
     * @param neigh
     *            the current pedestrian neighborhood
     */
    private void maximumpheromoneSearch(final Neighborhood<List<ILsaMolecule>> neigh) {
        double maxpheromone = 0.0;
        // For each node in the neighborhood
        for (final Node<List<ILsaMolecule>> node : neigh.getNeighbors()) {
            final ILsaNode n = (ILsaNode) node;
            List<ILsaMolecule> gradList;
            gradList = n.getConcentration(pheromoneTmpl);
            // Check if the current node has a gradient value
            if (!gradList.isEmpty()) {
                for (int i = 0; i < gradList.size(); i++) {
                    // Get current node gradient value
                    final double valuepheromone = getLSAArgumentAsDouble(gradList.get(i), 1);
                    // If the current minimum value is less than or equals
                    // current node gradient value
                    if (valuepheromone >= maxpheromone) {
                        // Update minimum gradient value node data
                        maxpheromone = valuepheromone;
                        targetPositions = getPosition(n);
                        bestNode = n;
                        if (getLSAArgumentAsDouble(n.getConcentration(template).get(i), gradDistPos) == 0) {
                            targetInLineOfSight = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Method used to execute a minumum gradient node search.
     * 
     * @param neigh
     *            - the current pedestrian neighborhood
     */
    private void minimumGradientSearch(final Neighborhood<List<ILsaMolecule>> neigh) {
        double minGrad = Double.MAX_VALUE;
        for (final Node<List<ILsaMolecule>> node : neigh.getNeighbors()) {
            final ILsaNode n = (ILsaNode) node;
            final List<ILsaMolecule> gradList = n.getConcentration(template);
            // Check if the current node has a gradient value
            if (!gradList.isEmpty()) {
                for (ILsaMolecule aGradList : gradList) {
                    // Get current node gradient value
                    final double valueGrad = getLSAArgumentAsDouble(aGradList, gradDistPos);
                    // If the current minimum value is less than or equals
                    // current node gradient value
                    if (valueGrad <= minGrad) {
                        // Update minimum gradient value node data
                        minGrad = valueGrad;
                        targetPositions = getPosition(n);
                        bestNode = n;
                        if (valueGrad == 0) {
                            targetInLineOfSight = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Method used in group behavior to update the pheromone value on a node
     * choose as target.
     */
    private void releasepheromone() {
        // Get the pheromone from the best node
        final List<ILsaMolecule> pheromonelist = bestNode.getConcentration(pheromoneTmpl);

        // If the best node is already visited
        if (isVisited((ILsaNode) bestNode)) {
            /*
             * If the best node (maximum pheromone value search) is already
             * visited,select the minimum gradient value search for the next
             * cycle
             */
            getMinimumGradient = true;
        } else {
            // Check if best node contains a pheromone value
            if (pheromonelist.isEmpty()) { // If no
                // Add a new pheromone concentration
                ((ILsaNode) bestNode).setConcentration(newPheromone);

            } else { // If yes

                // Get the pheromone concentration and update its value
                final ILsaMolecule pheromoneN = pheromonelist.get(0);
                if (Integer.parseInt(pheromoneN.getArg(2).toString()) == groupId) {
                    final int pheromoneNew = Integer.parseInt(pheromoneN.getArg(1).toString()) + 1;

                    ((ILsaNode) bestNode).removeConcentration(pheromoneTmpl);

                    final ILsaMolecule molNew = new LsaMolecule("pheromone, " + pheromoneNew + ", " + groupId);
                    ((ILsaNode) bestNode).setConcentration(molNew);
                }

            }
            // Select maximum pheromone value search
            getMinimumGradient = false;

        }
    }

}
