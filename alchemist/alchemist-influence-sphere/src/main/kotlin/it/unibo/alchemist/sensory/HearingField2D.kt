package it.unibo.alchemist.sensory

import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.shapes.GeometricShape2D
import java.awt.geom.Ellipse2D

class HearingField2D<P: Position2D<P>> (originX: Double,
                                        originY: Double,
                                        radius: Double)
    : InfluenceSphere2D<P>(GeometricShape2D<P>(Ellipse2D.Double(originX-radius, originY-radius,
                                                                radius*2, radius*2)))