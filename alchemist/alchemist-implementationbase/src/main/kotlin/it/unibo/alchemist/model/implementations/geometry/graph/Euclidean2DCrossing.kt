package it.unibo.alchemist.model.implementations.geometry.graph

import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DSegment
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdgeWithData

/**
 * Defines a connection between two [ConvexPolygon]s represented as an [Euclidean2DSegment].
 * The segment models the shape of the passage between the two areas, an agent could cross
 * any point of it to move from one room (or better, polygon) to the other.
 */
typealias Euclidean2DCrossing = GraphEdgeWithData<ConvexPolygon, Euclidean2DSegment>
