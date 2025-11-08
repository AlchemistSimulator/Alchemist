val incarnation = SAPERE.incarnation<Double, Euclidean2DPosition>()
simulation(incarnation) {
    layer {
        molecule = "A"
        layer = StepLayer(2.0, 2.0, 100.0, 0.0)
    }
    layer {
        molecule = "B"
        layer = StepLayer(-2.0, -2.0, 0.0, 100.0)
    }
    deployments {
        deploy(
            grid(-5.0, -5.0, 5.0, 5.0, 0.25,
                0.1, 0.1,
            ),
        ) {
            all {
                molecule = "a"
            }
        }
    }
}
