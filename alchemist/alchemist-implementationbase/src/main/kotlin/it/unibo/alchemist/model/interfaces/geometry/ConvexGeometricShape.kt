package it.unibo.alchemist.model.interfaces.geometry

/**
 * A convex shape.
 *
 * This interface models a property instead of an object, this may be
 * unusual but consider it as a contract: interfaces are often said to
 * be contracts the implementor has to comply, the contract defined by
 * this interface implies convexity.
 */
interface ConvexGeometricShape<V : Vector<V>, A : GeometricTransformation<V>> : GeometricShape<V, A>
