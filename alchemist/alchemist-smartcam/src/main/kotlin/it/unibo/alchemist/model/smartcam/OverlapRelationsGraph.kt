package it.unibo.alchemist.model.smartcam

/**
 * Models the partial view of each camera of the overlap relations graph
 * as described in "Online Multi-object k-coverage with Mobile Smart Cameras" by Lukas Esterle and Peter Lewis.
 * It is a map from the camera id to the relative link strength.
 * Links can be strengthen, increasing their values by [strengthenValue].
 * Links can be evaporated, multiplying their values with the factors [evaporationBaseFactor] and [evaporationMovementFactor].
 */
class OverlapRelationsGraph<C>(
    private val strengthenValue: Double,
    private val evaporationBaseFactor: Double,
    private val evaporationMovementFactor: Double
) {

    /**
     * Returns all the links in form of a map from the camera id to the link strength.
     */
    val links: Map<C, Double>
        get() = _links
    private val _links = mutableMapOf<C, Double>()

    /**
     * Increase link strength by [strengthenValue].
     */
    fun strengthenLink(camera: C) {
        _links.merge(camera, strengthenValue) { v1, v2 -> v1 + v2 }
    }

    /**
     * Decrease link strength by [evaporationBaseFactor] and additionally decreased it by [evaporationMovementFactor]
     * if [moved] is true.
     */
    fun evaporateAllLinks(moved: Boolean) {
        _links.replaceAll { _, value ->
            var newValue = value * evaporationBaseFactor
            if (moved) {
                newValue *= evaporationMovementFactor
            }
            newValue
        }
    }
}