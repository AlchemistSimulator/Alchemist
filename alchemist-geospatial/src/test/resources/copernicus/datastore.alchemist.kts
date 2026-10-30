import it.unibo.alchemist.boundary.kotlindsl.simulationOnMap

simulationOnMap(ProtelisIncarnation()) {
    environment(oSMEnvironment("maps/cesena.pbf")) {
        layer(
            "river-discharge",
            doubleGriddedGeoLayer(
                data = CopernicusData(
                    // The datastore API: CDS, EWDS, or ADS
                    endpoint = "https://ewds.climate.copernicus.eu/api",
                    dataset = "cems-glofas-historical",
                    // The request, as shown by the "API request" section of the dataset download form:
                    // a path, or the name of a file among the classpath resources
                    inputsFile = "copernicus/glofas-request.json",
                    // Optional: where the downloaded data is cached, by default in the user cache directory
                    cacheDirectory = "build/copernicus-cache",
                ),
                timeScale = "PT1H",
            ),
        )
        deployments {
            deploy(circle(10, 44.14, 12.24, 0.01))
        }
    }
}
