package it.unibo.alchemist.model.influencesphere.sensory

import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.influencesphere.shapes.GeometricShape2D
import java.awt.geom.Ellipse2D

class HearingField2D<P : Position2D<P>>(
    originX: Double,
    originY: Double,
    radius: Double = 2.0
) : InfluenceSphere2D<P>(GeometricShape2D<P>(
        Ellipse2D.Double(originX - radius, originY - radius, radius * 2, radius * 2)
))