/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.impl;

import it.unibo.alchemist.boundary.swingui.effect.api.Effect;
import it.unibo.alchemist.boundary.ui.api.Wormhole2D;
import it.unibo.alchemist.model.geometry.AwtShapeCompatible;
import it.unibo.alchemist.model.geometry.Shape;
import it.unibo.alchemist.model.actions.CameraSee;
import it.unibo.alchemist.model.molecules.SimpleMolecule;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position2D;
import it.unibo.alchemist.model.physics.properties.OccupiesSpaceProperty;
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment;
import org.jooq.lambda.function.Consumer2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;

/**
 * Draws node's shapes and cameras' fields of view.
 */
public final class DrawSmartcam implements Effect {
    private static final Logger LOGGER = LoggerFactory.getLogger(DrawSmartcam.class);
    private static final SimpleMolecule WANTED = new SimpleMolecule("wanted");
    private static final long serialVersionUID = 1L;
    private boolean alreadyLogged;

    @Override
    public <T, P extends Position2D<P>> void apply(
            final Graphics2D graphics,
            final Node<T> node,
            final Environment<T, P> environment,
            final Wormhole2D<P> wormhole
    ) {
        final double zoom = wormhole.getZoom();
        final Point viewPoint = wormhole.getViewPoint(environment.getPosition(node));
        final int x = viewPoint.x;
        final int y = viewPoint.y;
        if (environment instanceof Physics2DEnvironment) {
            @SuppressWarnings("unchecked")
            final Physics2DEnvironment<T> physicsEnvironment = (Physics2DEnvironment<T>) environment;
            drawShape(graphics, node, physicsEnvironment, zoom, x, y);
            drawFieldOfView(graphics, node, physicsEnvironment, zoom, x, y);
        } else {
            logOnce("DrawSmartcam only works with EuclideanPhysics2DEnvironment", Logger::warn);
        }
    }

    @Override
    public Color getColorSummary() {
        return Color.GREEN;
    }

    private <T> void drawShape(
            final Graphics2D graphics,
            final Node<T> node,
            final Physics2DEnvironment<T> environment,
            final double zoom,
            final int x,
            final int y
    ) {
        @SuppressWarnings("unchecked")
        final Shape<?, ?> geometricShape = node.asPropertyOrNull(OccupiesSpaceProperty.class) != null
                ? node.asProperty(OccupiesSpaceProperty.class).getShape()
                : null;
        if (geometricShape instanceof AwtShapeCompatible) {
            final AffineTransform transform = getTransform(x, y, zoom, getRotation(node, environment));
            final java.awt.Shape shape = transform.createTransformedShape(((AwtShapeCompatible) geometricShape).asAwtShape());
            if (node.contains(WANTED)) {
                graphics.setColor(Color.RED);
            } else {
                graphics.setColor(Color.GREEN);
            }
            graphics.draw(shape);
        } else {
            logOnce("DrawSmartcam only works with shapes implementing AwtShapeCompatible", Logger::warn);
        }
    }

    private <T> void drawFieldOfView(
            final Graphics2D graphics,
            final Node<T> node,
            final Physics2DEnvironment<T> environment,
            final double zoom,
            final int x,
            final int y
    ) {
        final AffineTransform transform = getTransform(x, y, zoom, getRotation(node, environment));
        graphics.setColor(Color.BLUE);
        node.getReactions()
            .stream()
            .flatMap(r -> r.getActions().stream())
            .filter(a -> a instanceof CameraSee)
            .map(a -> (CameraSee) a)
            .forEach(a -> {
                final double angle = a.getAngle();
                final double startAngle = -angle / 2;
                final double d = a.getDistance();
                final java.awt.Shape fov = new Arc2D.Double(-d, -d, d * 2, d * 2, startAngle, angle, Arc2D.PIE);
                graphics.draw(transform.createTransformedShape(fov));
            });
    }

    private <T> double getRotation(final Node<T> node, final Physics2DEnvironment<T> environment) {
        final Euclidean2DPosition direction = environment.getHeading(node);
        return Math.atan2(direction.getY(), direction.getX());
    }

    private AffineTransform getTransform(final int x, final int y, final double zoom, final double rotation) {
        final AffineTransform transform = new AffineTransform();
        transform.translate(x, y);
        transform.scale(zoom, zoom);
        transform.rotate(-rotation); // invert angle because the y axis is inverted in the gui
        return transform;
    }

    private void logOnce(final String message, final Consumer2<Logger, String> logger) {
        if (!alreadyLogged) {
            logger.accept(LOGGER, message);
            alreadyLogged = true;
        }
    }
}
