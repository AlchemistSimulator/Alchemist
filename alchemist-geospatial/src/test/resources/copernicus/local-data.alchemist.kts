import it.unibo.alchemist.boundary.kotlindsl.simulationOnMap

simulationOnMap(ProtelisIncarnation()) {
    environment(oSMEnvironment("maps/cesena.pbf")) {
        layer(
            "river-discharge",
            doubleCopernicusLayer(
                // A directory of NetCDF or GRIB files sharing the same variable and grid:
                // a path, or the name of a directory among the classpath resources
                dataDirectory = "copernicus/glofas-cesena",
                // Each simulated time unit lasts one hour of the data
                timeScale = "PT1H",
                // Simulation time zero corresponds to this instant (by default, the first instant in the data)
                timeOrigin = "2024-06-10T00:00:00Z",
                // The variable to read, optional if the files hold only one
                variable = "dis24",
            ),
        )
        deployments {
            deploy(circle(10, 44.14, 12.24, 0.01))
        }
    }
}
